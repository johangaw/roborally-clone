package ui

import com.soywiz.korge.view.*
import gamemodel.*
import kotlin.math.*

class CourseView(val course: Course, bitmapCache: BitmapCache) : Container() {
    private val COURSE_SIZE = 2048.0
    val cellSize: Double get() = min( COURSE_SIZE / course.width, COURSE_SIZE/course.height)

    init {
        repeat(course.width) { x ->
            repeat(course.height) { y ->
                image(bitmapCache.floor) {
                    size(cellSize, cellSize)
                    position(x * cellSize, y * cellSize)
                }
            }
        }
    }
}

fun Container.courseView(course: Course, bitmapCache: BitmapCache, callback: CourseView.() -> Unit = {}) =
    CourseView(course, bitmapCache)
        .addTo(this)
        .apply(callback)
