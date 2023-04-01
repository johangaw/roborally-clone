package ui

import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import gamemodel.*
import kotlin.math.*

class CourseBuilderScene: Scene() {

    private lateinit var course: Course
    private lateinit var courseView: View

    override suspend fun SContainer.sceneMain() {
        course = Course(10, 15, emptyList())

        val bitmapCache = BitmapCache.create()
        courseView = courseView(course, bitmapCache) {
            val scaleFactor = min(views.virtualWidthDouble / width, views.virtualHeightDouble / height)
            scale  = scaleFactor
            centerOn(this@sceneMain)
        }
    }
}
