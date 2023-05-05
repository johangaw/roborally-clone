import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import gamemodel.*
import ui.scenes.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()
    val course = loadCourseBuilderAutoSave()
    when (val setupResult = setupGame(course, 1)) {
        is SetupGameResult.Success -> {
            sceneContainer.changeTo({ GameScene(setupResult.gameModel) })
        }

        is SetupGameResult.NotEnoughStartingPositions -> {
            println("WARN: unable to setup game with ${setupResult.required} players, only ${setupResult.available} start positions available")
            sceneContainer.changeTo({ CourseBuilderScene(course) })
        }
    }

    suspend fun changeToGameSceneOrShowError(course: Course, playerCount: Int) {
        when (val res = setupGame(course, playerCount)) {
            is SetupGameResult.Success -> sceneContainer.changeTo({ GameScene(res.gameModel) })
            is SetupGameResult.NotEnoughStartingPositions -> println("WARN: unable to setup game with ${res.required} players, only ${res.available} start positions available") // TODO show error
        }
    }

    suspend fun changeToGameSceneOrShowError(course: PreBuildCourse, playerCount: Int) {
        changeToGameSceneOrShowError(loadCourse(course), playerCount)
    }

    keys {
        down {
            when (it.key) {
                Key.N1 -> changeToGameSceneOrShowError(PreBuildCourse.Course1, 1)
                Key.N2 -> changeToGameSceneOrShowError(PreBuildCourse.CoursePalette, 1)

                Key.N9 -> sceneContainer.changeTo({ CourseBuilderScene() })
                Key.N0 -> when (val scene = sceneContainer.currentScene) {
                    is GameScene -> sceneContainer.changeTo({ CourseBuilderScene(scene.gameModel.course) })
                    is CourseBuilderScene -> changeToGameSceneOrShowError(scene.course, 1)
                }

                else -> Unit
            }
        }
    }
}


