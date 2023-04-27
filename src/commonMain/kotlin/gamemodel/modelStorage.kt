package gamemodel

import com.soywiz.korio.file.std.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

enum class PreBuildCourse {
    Course1,
    CoursePalette,
}


fun serialize(gameModel: GameModel): String {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.encodeToString(gameModel)
}

fun deserializeGameModel(gameModel: String): GameModel {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.decodeFromString(gameModel)
}

fun serialize(course: Course): String {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.encodeToString(course)
}

fun deserializeCourse(gameModel: String): Course {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.decodeFromString(gameModel)
}


suspend fun loadCourse(course: PreBuildCourse): Course {
    val json = Json {
        allowStructuredMapKeys = true
    }
    return json.decodeFromString(
        when(course) {
            PreBuildCourse.Course1 -> resourcesVfs["courses/course1.json"]
            PreBuildCourse.CoursePalette -> resourcesVfs["courses/course_palette.json"]
        }.readString()
    )
}

const val COURSE_BUILDER_AUTO_SAVE_FILE = "course_builder_auto_save.json"
suspend fun storeCourseBuilderAutoSave(course: Course) {
    resourcesVfs["courses/dynamic/${COURSE_BUILDER_AUTO_SAVE_FILE}"].writeString(serialize(course))
}

suspend fun loadCourseBuilderAutoSave(): Course {
    return deserializeCourse(resourcesVfs["courses/dynamic/${COURSE_BUILDER_AUTO_SAVE_FILE}"].readString())
}
