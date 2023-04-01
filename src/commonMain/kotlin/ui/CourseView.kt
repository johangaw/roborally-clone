package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*
import kotlin.math.*

class CourseView(val course: Course, bitmapCache: BitmapCache) : Container() {
    private val COURSE_SIZE = 2048.0
    val cellSize: Double get() = scaledWidth / course.width

    private var onClickHandler: (pos: Pos) -> Unit = {}

    init {
        val cellSize = min(COURSE_SIZE / course.width, COURSE_SIZE / course.height)
        repeat(course.width) { x ->
            repeat(course.height) { y ->
                image(bitmapCache.floor) {
                    size(cellSize, cellSize)
                    position(x * cellSize, y * cellSize)
                    onClick { onClickHandler(Pos(x, y)) }
                }
            }
        }

        course.conveyorBelts.forEach {
            conveyorBeltView(it.type, bitmapCache) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(it.pos))
            }
        }
    }

    fun getPoint(pos: Pos): IPoint = Point(pos.x * cellSize, pos.y * cellSize)

    fun onClick(handler: (pos: Pos) -> Unit) {
        onClickHandler = handler
    }
}

fun Container.courseView(course: Course, bitmapCache: BitmapCache, callback: CourseView.() -> Unit = {}) =
    CourseView(course, bitmapCache)
        .addTo(this)
        .apply(callback)
