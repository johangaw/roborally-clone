package ui

import com.soywiz.kmem.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.math.*

val INITIAL_COURSE = Course(
    width = 10,
    height = 15,
    conveyorBelts = mapOf(
        Pos(2, 3) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
        Pos(3, 3) to ConveyorBelt(ConveyorBeltType.RightAndDown, ConveyorBeltSpeed.Regular),
        Pos(3, 4) to ConveyorBelt(ConveyorBeltType.Down, ConveyorBeltSpeed.Regular),
        Pos(3, 5) to ConveyorBelt(ConveyorBeltType.DownAndLeft, ConveyorBeltSpeed.Regular),
        Pos(2, 5) to ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular),
        Pos(1, 5) to ConveyorBelt(ConveyorBeltType.LeftAndUp, ConveyorBeltSpeed.Regular),
        Pos(1, 4) to ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular),
        Pos(1, 3) to ConveyorBelt(ConveyorBeltType.UpAndRight, ConveyorBeltSpeed.Regular),

        Pos(7, 3) to ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular),
        Pos(6, 3) to ConveyorBelt(ConveyorBeltType.LeftAndDown, ConveyorBeltSpeed.Regular),
        Pos(6, 4) to ConveyorBelt(ConveyorBeltType.Down, ConveyorBeltSpeed.Regular),
        Pos(6, 5) to ConveyorBelt(ConveyorBeltType.DownAndRight, ConveyorBeltSpeed.Regular),
        Pos(7, 5) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
        Pos(8, 5) to ConveyorBelt(ConveyorBeltType.RightAndUp, ConveyorBeltSpeed.Regular),
        Pos(8, 4) to ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular),
        Pos(8, 3) to ConveyorBelt(ConveyorBeltType.UpAndLeft, ConveyorBeltSpeed.Regular),
    ),
    walls = listOf(
        Wall(Pos(1, 7), Direction.Up),
        Wall(Pos(2, 7), Direction.Up),
        Wall(Pos(3, 7), Direction.Up),

        Wall(Pos(3, 7), Direction.Right),
        Wall(Pos(3, 8), Direction.Right),

        Wall(Pos(1, 7), Direction.Left),
        Wall(Pos(1, 8), Direction.Left),

        Wall(Pos(1, 8), Direction.Down),
        Wall(Pos(2, 8), Direction.Down),
        Wall(Pos(3, 8), Direction.Down),
    ),
    checkpoints = listOf(
        Checkpoint(CheckpointId(1), Pos(2, 1)),
        Checkpoint(CheckpointId(2), Pos(3, 1)),
        Checkpoint(CheckpointId(3), Pos(4, 1)),
        Checkpoint(CheckpointId(4), Pos(5, 1)),
        Checkpoint(CheckpointId(5), Pos(6, 1)),
        Checkpoint(CheckpointId(6), Pos(7, 1)),
    ),
    starts = listOf(
        Start(Pos(1, 10), 1),
        Start(Pos(2, 10), 2),
        Start(Pos(3, 10), 3),
        Start(Pos(4, 10), 4),
        Start(Pos(5, 10), 5),
        Start(Pos(6, 10), 6),
        Start(Pos(7, 10), 7),
        Start(Pos(8, 10), 8),
    ),
)

sealed class ControlElement {
    data class ConveyorBelt(val type: ConveyorBeltType) : ControlElement()

    data class Wall(val dir: Direction) : ControlElement()

    data class Checkpoint(val id: CheckpointId) : ControlElement()

    data class Start(val order: Int) : ControlElement()
}

class CourseBuilderScene : Scene() {

    private lateinit var bitmapCache: BitmapCache
    private lateinit var coursePanel: Container
    private lateinit var courseView: View
    private lateinit var controlElementViews: Map<ControlElement, RoundRect>
    private var selectedControlElement: ControlElement? = null
    private var course: Course = INITIAL_COURSE
        set(value) {
            field = value
            sceneContainer.redrawCourse()
        }

    override suspend fun SContainer.sceneMain() {
        bitmapCache = BitmapCache.create()
        val controlPanelWidth = 120.0

        fun Container.controlPanelElement(element: ControlElement, callback: RoundRect.() -> Unit) = roundRect(
                controlPanelWidth / 2, controlPanelWidth / 2, 0.0, fill = Colors.TRANSPARENT_WHITE
            ).addTo(this).apply {
                onClick { selectControlElement(element) }
                this.callback()
            }

        val controlPanel = roundRect(controlPanelWidth, views.virtualHeightDouble, 2.0, fill = Colors.LIGHTGRAY) {
            alignLeftToLeftOf(this@sceneMain)

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
                    (1..6)
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


            roundRect(75.0, 40.0, 3.0, fill = Colors.DARKGRAY) {
                text("SAVE") {
                    color = Colors.WHITE
                    centerOn(parent!!)
                }
                onClick { saveCourse() }
                centerOn(parent!!)
                alignBottomToBottomOf(parent!!)
            }

        }

        coursePanel = fixedSizeContainer(views.virtualWidthDouble - controlPanelWidth, views.virtualHeightDouble) {
            alignLeftToRightOf(controlPanel)
        }
        redrawCourse()
    }

    private fun saveCourse() {
        val json = Json {
            allowStructuredMapKeys = true
        }
        val str = json.encodeToString(course)
        println(str)
    }


    private fun Container.redrawCourse() {
        coursePanel.removeAllComponents()
        courseView = courseView(course, bitmapCache) {
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
            else if(previousStartAtPos != null) {
                course.starts.filter { it.order != startOrder } - previousStartAtPos + newStart
            }
            else
                course.starts.filter { it.order != startOrder } + newStart
        )
    }

    private fun handlePosClick(pos: Pos, id: CheckpointId) {
        val newCheckpoint = Checkpoint(id, pos)
        val previousCheckpointAtPos = course.checkpoints.firstOrNull { it.pos == pos }

        course = course.copy(
            checkpoints = if (previousCheckpointAtPos?.id == id)
                course.checkpoints - previousCheckpointAtPos
            else if(previousCheckpointAtPos != null) {
                course.checkpoints.filter { it.id != id } - previousCheckpointAtPos + newCheckpoint
            }
            else
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
