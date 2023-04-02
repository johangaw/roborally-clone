package ui

import com.soywiz.kmem.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
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
        Wall(Pos(1,7), Direction.Up),
        Wall(Pos(2,7), Direction.Up),
        Wall(Pos(3,7), Direction.Up),

        Wall(Pos(3,7), Direction.Right),
        Wall(Pos(3,8), Direction.Right),

        Wall(Pos(1,7), Direction.Left),
        Wall(Pos(1,8), Direction.Left),

        Wall(Pos(1,8), Direction.Down),
        Wall(Pos(2,8), Direction.Down),
        Wall(Pos(3,8), Direction.Down),
    ),
)

sealed class ControlElement{
    data class ConveyorBelt(val type: ConveyorBeltType): ControlElement()
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
        val controlPanel = roundRect(controlPanelWidth, views.virtualHeightDouble, 2.0, fill = Colors.LIGHTGRAY) {
            alignLeftToLeftOf(this@sceneMain)
            val controlElementPadding = 0

            controlElementViews = ConveyorBeltType
                .values()
                .map { type ->
                    val element = ControlElement.ConveyorBelt(type)
                    element to roundRect(
                        controlPanelWidth / 2, controlPanelWidth / 2, 0.0, fill = Colors.TRANSPARENT_WHITE
                    ) {
                        onClick { selectControlElement(element) }
                        conveyorBeltView(type, bitmapCache) {
                            setSizeScaled(50.0, 50.0)
                            centerOn(parent!!)
                        }
                    }
                }
                .also { controls ->
                    val (left, right) = controls
                        .map { it.second }
                        .withIndex()
                        .partition { it.index.isEven }
                    left.first().value.apply {
                        alignTopToTopOf(this@roundRect, controlElementPadding)
                        alignLeftToLeftOf(this@roundRect, controlElementPadding)
                    }
                    right.first().value.apply {
                        alignTopToTopOf(this@roundRect, controlElementPadding)
                        alignRightToRightOf(this@roundRect, controlElementPadding)
                    }

                    val alignUnderPrevious = { v1: View, v2: View ->
                        v2.alignLeftToLeftOf(v1)
                        v2.alignTopToBottomOf(v1, controlElementPadding)
                    }

                    left
                        .map { it.value }
                        .zipWithNext(alignUnderPrevious)
                    right
                        .map { it.value }
                        .zipWithNext(alignUnderPrevious)
                }
                .toMap()

        }

        coursePanel = fixedSizeContainer(views.virtualWidthDouble - controlPanelWidth, views.virtualHeightDouble) {
            alignLeftToRightOf(controlPanel)
        }
        redrawCourse()
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
        when(val element = selectedControlElement){
            is ControlElement.ConveyorBelt -> handlePosClick(pos, element)
            else -> Unit
        }
    }

    private fun handlePosClick(pos: Pos, belt: ControlElement.ConveyorBelt) {
        course = course.copy(
            conveyorBelts = course.conveyorBelts + (pos to ConveyorBelt(
                belt.type, ConveyorBeltSpeed.Regular
            ))
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
