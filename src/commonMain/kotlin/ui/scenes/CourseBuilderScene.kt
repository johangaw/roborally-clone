package ui.scenes

import com.soywiz.kmem.*
import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.*
import gamemodel.*
import ui.*
import java.lang.Exception
import kotlin.math.*

enum class ConveyorBeltControlElementType(val iconType: ConveyorBeltType) {
    Straight(gamemodel.ConveyorBeltType.Right), RightTurn(gamemodel.ConveyorBeltType.RightAndDown), LeftTurn(gamemodel.ConveyorBeltType.RightAndUp)
}

sealed class ControlElement(val rotatable: Boolean) {
    data class ConveyorBelt(val type: ConveyorBeltControlElementType, val speed: ConveyorBeltSpeed) :
        ControlElement(true)

    object Wall : ControlElement(true)

    data class LaserCannon(val power: Int) : ControlElement(true)

    data class Checkpoint(val id: CheckpointId) : ControlElement(false)

    data class Start(val order: Int) : ControlElement(false)

    object Pit : ControlElement(false)

}

class CourseBuilderScene(private val initialCourse: Course? = null) : Scene() {

    private lateinit var bitmapCache: BitmapCache
    private lateinit var coursePanel: Container
    private lateinit var controlElementViews: Map<ControlElement, RoundRect>
    private var courseView: View? = null
    private var selectedControlElement: ControlElement? = null
    private var controlElementDirection: Direction = Direction.Right

    var course: Course = initialCourse ?: Course(0, 0)
        private set(value) {
            field = value
            redrawCourse()
            launch {
                storeCourseBuilderAutoSave(value)
            }
        }

    override suspend fun SContainer.sceneInit() {
        bitmapCache = BitmapCache.create()
        val controlPanelWidth = 120.0

        fun Container.controlPanelElement(element: ControlElement, callback: RoundRect.() -> Unit) = roundRect(
            controlPanelWidth / 2, controlPanelWidth / 2, 0.0, fill = Colors.TRANSPARENT_WHITE
        )
            .addTo(this)
            .apply {
                onClick { selectControlElement(element) }
                this.callback()
            }

        val controlPanel = roundRect(controlPanelWidth, views.virtualHeightDouble, 2.0, fill = Colors.LIGHTGRAY) {
            alignLeftToLeftOf(this@sceneInit)

            controlElementViews = emptyList<Pair<ControlElement, RoundRect>>()
                .asSequence()
                .plus(ConveyorBeltControlElementType
                          .values()
                          .toList()
                          .cartesianProduct(
                              ConveyorBeltSpeed
                                  .values()
                                  .toList()
                          )
                          .map { ControlElement.ConveyorBelt(it.first, it.second) }
                          .map {
                              it to controlPanelElement(it) {
                                  conveyorBeltView(it.type.iconType, it.speed, bitmapCache) {
                                      setSizeScaled(50.0, 50.0)
                                      centerOn(parent!!)
                                  }
                              }
                          }
                )
                .plus(ControlElement.Wall to controlPanelElement(ControlElement.Wall) {
                    image(bitmapCache.floor) {
                        setSizeScaled(50.0, 50.0)
                        centerOn(parent!!)
                    }
                    wallView(bitmapCache, Direction.Left) {
                        setSizeScaled(50.0, 50.0)
                        centerOn(parent!!)
                    }
                })
                .plus((1..3)
                          .map { ControlElement.LaserCannon(it) }
                          .map {
                              it to controlPanelElement(it) {
                                  image(bitmapCache.floor) {
                                      setSizeScaled(50.0, 50.0)
                                      centerOn(parent!!)
                                  }
                                  laserCannonView(bitmapCache, Direction.Right, it.power) {
                                      setSizeScaled(50.0, 50.0)
                                      centerOn(parent!!)
                                  }
                              }
                          })
                .plus(ControlElement.Pit.let {
                    it to controlPanelElement(it) {
                        pitView(SurroundingPits.ALONE_PIT, bitmapCache) {
                            setSizeScaled(50.0, 50.0)
                            centerOn(parent!!)
                        }
                    }
                })
                .plus((1..6)
                          .map { ControlElement.Checkpoint(CheckpointId(it)) }
                          .map {
                              it to controlPanelElement(it) {
                                  checkpointView(it.id, bitmapCache) {
                                      setSizeScaled(50.0, 50.0)
                                      centerOn(parent!!)
                                  }
                              }
                          })
                .plus((1..8).map {
                    ControlElement.Start(it) to controlPanelElement(ControlElement.Start(it)) {
                        startView(it) {
                            setSizeScaled(50.0, 50.0)
                            centerOn(parent!!)
                        }
                    }
                })
                .also { controls ->
                    val (left, right) = controls
                        .map { it.second }
                        .withIndex()
                        .partition { it.index.isEven }
                    left.first().value.apply {
                        alignTopToTopOf(this@roundRect)
                        alignLeftToLeftOf(this@roundRect)
                    }
                    right.first().value.apply {
                        alignTopToTopOf(this@roundRect)
                        alignRightToRightOf(this@roundRect)
                    }

                    val alignUnderPrevious = { v1: View, v2: View ->
                        v2.alignLeftToLeftOf(v1)
                        v2.alignTopToBottomOf(v1)
                    }

                    left
                        .map { it.value }
                        .zipWithNext(alignUnderPrevious)
                    right
                        .map { it.value }
                        .zipWithNext(alignUnderPrevious)
                }
                .toMap()


            roundRect(75.0, 40.0, 3.0, fill = Colors.GREEN) {
                text("PRINT") {
                    color = Colors.BLACK
                    centerOn(parent!!)
                }
                onClick { printCourse() }
                centerOn(parent!!)
                alignBottomToBottomOf(parent!!, 4.0)
            }

        }

        coursePanel = fixedSizeContainer(views.virtualWidthDouble - controlPanelWidth, views.virtualHeightDouble) {
            alignLeftToRightOf(controlPanel)
        }

        if (initialCourse == null) loadLastCourse()
        redrawCourse()

        keys {
            down {
                when (it.key) {
                    Key.N -> {
                        course = Course(12, 16)
                    }

                    Key.ESCAPE -> {
                        printCourse()
                    }

                    Key.R -> rotateControlElements()

                    else -> Unit
                }
            }
        }
    }

    private fun rotateControlElements() {
        controlElementDirection = controlElementDirection.quoter()
        controlElementViews.filter { it.key.rotatable }.values.forEach {

            val dx = it.globalBounds.x + it.scaledWidth / 2
            val dy = it.globalBounds.y + it.scaledHeight / 2

            it.setTransform(
                it.localMatrix
                    .translate(-dx, -dy)
                    .rotate(Angle.QUARTER)
                    .translate(dx, dy)
                    .toTransform()
            )
        }
    }

    private suspend fun loadLastCourse() {
        course = try {
            loadCourseBuilderAutoSave()
        } catch (e: Exception) {
            course
        }
    }

    private fun printCourse() {
        println(serialize(course))
    }

    private fun redrawCourse() {
        if (!this::coursePanel.isInitialized) return

        coursePanel.removeChildren()
        courseView = coursePanel.courseView(course, bitmapCache) {
            val scaleFactor = min(views.virtualWidthDouble / width, views.virtualHeightDouble / height)
            scale = scaleFactor
            onClick(::handlePosClick)
            centerOn(coursePanel)
        }
    }

    private fun handlePosClick(pos: Pos) {
        when (val element = selectedControlElement) {
            is ControlElement.ConveyorBelt -> handlePosClick(pos, controlElementDirection, element)
            is ControlElement.Wall -> handlePosClick(pos, controlElementDirection.opposite(), element)
            is ControlElement.LaserCannon -> handlePosClick(pos, controlElementDirection, element)
            is ControlElement.Checkpoint -> handlePosClick(pos, element)
            is ControlElement.Start -> handlePosClick(pos, element)
            is ControlElement.Pit -> handlePosClick(pos, element)
            null -> Unit
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handlePosClick(pos: Pos, element: ControlElement.Pit) {
        course = course.copy(
            pits = if (pos in course.pits) course.pits - pos else course.pits + pos
        )
    }

    private fun handlePosClick(pos: Pos, dir: Direction, element: ControlElement.LaserCannon) {
        val newCannon = LaserCannon(pos, dir, element.power)
        val cannonAtPos = course.laserCannons.firstOrNull { it.pos == pos }

        course = course.copy(
            laserCannons = if (cannonAtPos == newCannon) course.laserCannons - cannonAtPos
            else if (cannonAtPos != null) course.laserCannons - cannonAtPos + newCannon
            else course.laserCannons + newCannon
        )
    }

    private fun handlePosClick(pos: Pos, element: ControlElement.Start) {
        val newStart = Start(pos, element.order)
        val previousStartAtPos = course.starts.firstOrNull { it.pos == pos }

        course = course.copy(starts = if (previousStartAtPos?.order == element.order) course.starts - previousStartAtPos
        else if (previousStartAtPos != null) {
            course.starts.filter { it.order != element.order } - previousStartAtPos + newStart
        } else course.starts.filter { it.order != element.order } + newStart)
    }

    private fun handlePosClick(pos: Pos, element: ControlElement.Checkpoint) {
        val newCheckpoint = Checkpoint(element.id, pos)
        val previousCheckpointAtPos = course.checkpoints.firstOrNull { it.pos == pos }

        course =
            course.copy(checkpoints = if (previousCheckpointAtPos?.id == element.id) course.checkpoints - previousCheckpointAtPos
            else if (previousCheckpointAtPos != null) {
                course.checkpoints.filter { it.id != element.id } - previousCheckpointAtPos + newCheckpoint
            } else course.checkpoints.filter { it.id != element.id } + newCheckpoint)
    }

    private fun handlePosClick(pos: Pos, dir: Direction, element: ControlElement.ConveyorBelt) {

        val newBelt = ConveyorBelt(getConveyorBeltType(dir, element), element.speed)

        course = course.copy(
            conveyorBelts = if (course.conveyorBelts[pos] == newBelt) course.conveyorBelts - pos
            else course.conveyorBelts + (pos to newBelt)
        )
    }

    private fun getConveyorBeltType(dir: Direction, element: ControlElement.ConveyorBelt): ConveyorBeltType =
        when (element.type) {
            ConveyorBeltControlElementType.Straight -> when (dir) {
                Direction.Up -> ConveyorBeltType.Up
                Direction.Down -> ConveyorBeltType.Down
                Direction.Right -> ConveyorBeltType.Right
                Direction.Left -> ConveyorBeltType.Left
            }

            ConveyorBeltControlElementType.RightTurn -> when (dir) {
                Direction.Right -> ConveyorBeltType.RightAndDown
                Direction.Down -> ConveyorBeltType.DownAndLeft
                Direction.Left -> ConveyorBeltType.LeftAndUp
                Direction.Up -> ConveyorBeltType.UpAndRight
            }

            ConveyorBeltControlElementType.LeftTurn -> when (dir) {
                Direction.Right -> ConveyorBeltType.RightAndUp
                Direction.Down -> ConveyorBeltType.DownAndRight
                Direction.Left -> ConveyorBeltType.LeftAndDown
                Direction.Up -> ConveyorBeltType.UpAndLeft
            }
        }

    @Suppress("UNUSED_PARAMETER")
    private fun handlePosClick(pos: Pos, dir: Direction, element: ControlElement.Wall) {
        val newWall = Wall(pos, dir)
        val previousWall = course.walls.firstOrNull { wall: Wall -> wall.pos == newWall.pos && wall.dir == newWall.dir }
        course = course.copy(
            walls = if (previousWall != null) course.walls - previousWall
            else course.walls + newWall
        )
    }

    private fun selectControlElement(element: ControlElement) {
        controlElementViews.values.forEach {
            it.fill = Colors.TRANSPARENT_BLACK
        }
        controlElementViews.getValue(element).fill = Colors.RED
        selectedControlElement = element
    }
}
