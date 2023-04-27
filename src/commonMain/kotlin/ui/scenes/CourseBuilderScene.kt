package ui.scenes

import com.soywiz.kmem.*
import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import gamemodel.*
import ui.*
import java.lang.Exception
import kotlin.math.*

sealed class ControlElement {
    data class ConveyorBelt(val type: ConveyorBeltType) : ControlElement()

    data class Wall(val dir: Direction) : ControlElement()

    data class Checkpoint(val id: CheckpointId) : ControlElement()

    data class Start(val order: Int) : ControlElement()
}

class CourseBuilderScene(private val initialCourse: Course? = null) : Scene() {

    private lateinit var bitmapCache: BitmapCache
    private lateinit var coursePanel: Container
    private lateinit var controlElementViews: Map<ControlElement, RoundRect>
    private var courseView: View? = null
    private var selectedControlElement: ControlElement? = null
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
                .plus(
                    ConveyorBeltType
                        .values()
                        .map { type ->
                            ControlElement.ConveyorBelt(type) to controlPanelElement(ControlElement.ConveyorBelt(type)) {
                                conveyorBeltView(type, bitmapCache) {
                                    setSizeScaled(50.0, 50.0)
                                    centerOn(parent!!)
                                }
                            }
                        }
                )
                .plus(
                    Direction
                        .values()
                        .map { dir ->
                            ControlElement.Wall(dir) to controlPanelElement(ControlElement.Wall(dir)) {
                                image(bitmapCache.floor) {
                                    setSizeScaled(50.0, 50.0)
                                    centerOn(parent!!)
                                }
                                wallView(bitmapCache, dir) {
                                    setSizeScaled(50.0, 50.0)
                                    centerOn(parent!!)
                                }
                            }
                        }
                )
                .plus(
                    (1..6)
                        .map { CheckpointId(it) }
                        .map {
                            ControlElement.Checkpoint(it) to controlPanelElement(ControlElement.Checkpoint(it)) {
                                checkpointView(it, bitmapCache) {
                                    setSizeScaled(50.0, 50.0)
                                    centerOn(parent!!)
                                }
                            }
                        }
                )
                .plus(
                    (1..8)
                        .map {
                            ControlElement.Start(it) to controlPanelElement(ControlElement.Start(it)) {
                                startView(it) {
                                    setSizeScaled(50.0, 50.0)
                                    centerOn(parent!!)
                                }
                            }
                        }
                )
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

        if(initialCourse == null) loadLastCourse()
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

                    else -> Unit
                }
            }
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
        if(!this::coursePanel.isInitialized) return

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
            is ControlElement.ConveyorBelt -> handlePosClick(pos, element.type)
            is ControlElement.Wall -> handlePosClick(pos, element.dir)
            is ControlElement.Checkpoint -> handlePosClick(pos, element.id)
            is ControlElement.Start -> handlePosClick(pos, element.order)
            null -> Unit
        }
    }

    private fun handlePosClick(pos: Pos, startOrder: Int) {
        val newStart = Start(pos, startOrder)
        val previousStartAtPos = course.starts.firstOrNull { it.pos == pos }

        course = course.copy(
            starts = if (previousStartAtPos?.order == startOrder)
                course.starts - previousStartAtPos
            else if (previousStartAtPos != null) {
                course.starts.filter { it.order != startOrder } - previousStartAtPos + newStart
            } else
                course.starts.filter { it.order != startOrder } + newStart
        )
    }

    private fun handlePosClick(pos: Pos, id: CheckpointId) {
        val newCheckpoint = Checkpoint(id, pos)
        val previousCheckpointAtPos = course.checkpoints.firstOrNull { it.pos == pos }

        course = course.copy(
            checkpoints = if (previousCheckpointAtPos?.id == id)
                course.checkpoints - previousCheckpointAtPos
            else if (previousCheckpointAtPos != null) {
                course.checkpoints.filter { it.id != id } - previousCheckpointAtPos + newCheckpoint
            } else
                course.checkpoints.filter { it.id != id } + newCheckpoint
        )
    }

    private fun handlePosClick(pos: Pos, conveyorBeltType: ConveyorBeltType) {
        val newBelt = ConveyorBelt(conveyorBeltType, ConveyorBeltSpeed.Regular)

        course = course.copy(
            conveyorBelts = if (course.conveyorBelts[pos] == newBelt)
                course.conveyorBelts - pos
            else
                course.conveyorBelts + (pos to newBelt)
        )
    }

    private fun handlePosClick(pos: Pos, wallDirection: Direction) {
        val newWall = Wall(pos, wallDirection)
        val previousWall = course.walls.firstOrNull { wall: Wall -> wall.pos == newWall.pos && wall.dir == newWall.dir }
        course = course.copy(
            walls = if (previousWall != null)
                course.walls - previousWall
            else
                course.walls + newWall
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