package ui

import com.soywiz.kmem.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlin.math.*

val INITIAL_COURSE = Course(10, 15, listOf(
    ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular, Pos(2,3)),
    ConveyorBelt(ConveyorBeltType.RightAndDown, ConveyorBeltSpeed.Regular, Pos(3,3)),
    ConveyorBelt(ConveyorBeltType.Down, ConveyorBeltSpeed.Regular, Pos(3,4)),
    ConveyorBelt(ConveyorBeltType.DownAndLeft, ConveyorBeltSpeed.Regular, Pos(3,5)),
    ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular, Pos(2,5)),
    ConveyorBelt(ConveyorBeltType.LeftAndUp, ConveyorBeltSpeed.Regular, Pos(1,5)),
    ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular, Pos(1,4)),
    ConveyorBelt(ConveyorBeltType.UpAndRight, ConveyorBeltSpeed.Regular, Pos(1,3)),

    ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular, Pos(7,3)),
    ConveyorBelt(ConveyorBeltType.LeftAndDown, ConveyorBeltSpeed.Regular, Pos(6,3)),
    ConveyorBelt(ConveyorBeltType.Down, ConveyorBeltSpeed.Regular, Pos(6,4)),
    ConveyorBelt(ConveyorBeltType.DownAndRight, ConveyorBeltSpeed.Regular, Pos(6,5)),
    ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular, Pos(7,5)),
    ConveyorBelt(ConveyorBeltType.RightAndUp, ConveyorBeltSpeed.Regular, Pos(8,5)),
    ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular, Pos(8,4)),
    ConveyorBelt(ConveyorBeltType.UpAndLeft, ConveyorBeltSpeed.Regular, Pos(8,3)),
))

class CourseBuilderScene: Scene() {

    private lateinit var bitmapCache: BitmapCache
    private lateinit var coursePanel: Container
    private lateinit var courseView: View
    private lateinit var controlElementViews: Map<ConveyorBeltType, RoundRect>
    private lateinit var selectedControlElement : ConveyorBeltType
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

            controlElementViews = ConveyorBeltType.values().map { type ->
                type to roundRect(controlPanelWidth / 2, controlPanelWidth / 2, 0.0, fill = Colors.TRANSPARENT_WHITE) {
                    onClick { selectControlElement(type) }
                    conveyorBeltView(type, bitmapCache) {
                        setSizeScaled(50.0, 50.0)
                        centerOn(parent!!)
                    }
                }
            }.apply {
                val (left, right) = this.map { it.second }.withIndex().partition { it.index.isEven }
                left.first().value.apply {
                    alignTopToTopOf(this@roundRect, controlElementPadding)
                    alignLeftToLeftOf(this@roundRect, controlElementPadding)
                }
                right.first().value.apply {
                    alignTopToTopOf(this@roundRect, controlElementPadding)
                    alignRightToRightOf(this@roundRect, controlElementPadding)
                }

                val alignUnderPrevious = {v1: View, v2: View ->
                    v2.alignLeftToLeftOf(v1)
                    v2.alignTopToBottomOf(v1, controlElementPadding)
                }

                left.map { it.value }.zipWithNext(alignUnderPrevious)
                right.map { it.value }.zipWithNext(alignUnderPrevious)
            }.toMap()

        }
        selectControlElement(ConveyorBeltType.values().first())

        coursePanel = fixedSizeContainer(views.virtualWidthDouble - controlPanelWidth, views.virtualHeightDouble) {
            alignLeftToRightOf(controlPanel)
        }
        redrawCourse()
    }

    private fun Container.redrawCourse() {
        coursePanel.removeAllComponents()
        courseView = courseView(course, bitmapCache) {
            val scaleFactor = min(views.virtualWidthDouble / width, views.virtualHeightDouble / height)
            scale  = scaleFactor
            onClick(::handlePosClick)
            centerOn(coursePanel)
        }
    }

    private fun handlePosClick(pos: Pos) {
        course = course.copy(
            conveyorBelts = course.conveyorBelts.filter { it.pos != pos } + ConveyorBelt(selectedControlElement, ConveyorBeltSpeed.Regular, pos)
        )
    }

    private fun selectControlElement(type: ConveyorBeltType) {
        controlElementViews.values.forEach {
            it.fill = Colors.TRANSPARENT_BLACK
        }
        controlElementViews.getValue(type).fill = Colors.RED
        selectedControlElement = type
    }
}
