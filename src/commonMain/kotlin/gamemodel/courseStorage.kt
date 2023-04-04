package gamemodel

import com.soywiz.korio.file.std.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

enum class PreBuildCourses {
    Course1,
    CoursePalette,
}

fun serializeCourse(course: Course): String {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.encodeToString(course)
}

suspend fun loadCourse(course: PreBuildCourses): Course {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.decodeFromString(
        when(course) {
            PreBuildCourses.Course1 -> resourcesVfs["courses/course1.json"]
            PreBuildCourses.CoursePalette -> resourcesVfs["courses/course_palette.json"]
        }.readString()
    )
}
