package ui

import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlin.math.*

class CourseBuilderScene: Scene() {

    private lateinit var course: Course
    private lateinit var courseView: View

    override suspend fun SContainer.sceneMain() {
        course = Course(10, 15, listOf(
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

        val bitmapCache = BitmapCache.create()

        val controlPanelWidth = 100.0
        val controlPanel = roundRect(controlPanelWidth, views.virtualHeightDouble, 2.0, fill = Colors.LIGHTGRAY) {
            alignLeftToLeftOf(this@sceneMain)
        }

        val coursePanel = fixedSizeContainer(views.virtualWidthDouble - controlPanelWidth, views.virtualHeightDouble) {
            alignLeftToRightOf(controlPanel)
            courseView = courseView(course, bitmapCache) {
                val scaleFactor = min(views.virtualWidthDouble / width, views.virtualHeightDouble / height)
                scale  = scaleFactor
                onClick { println(it) }
                centerOn(this@fixedSizeContainer)
            }
        }
    }
}
