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
