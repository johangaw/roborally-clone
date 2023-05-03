import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import gamemodel.*
import ui.scenes.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val course1Game = setupGame(PreBuildCourse.Course1, playerCount = 1)
    val sceneContainer = sceneContainer()

    sceneContainer.changeTo({ GameScene(setupGame(loadCourseBuilderAutoSave(), 1)) })

    keys {
        down {
            when (it.key) {
                Key.N1 -> sceneContainer.changeTo({ GameScene(course1Game) })
                Key.N2 -> sceneContainer.changeTo({ GameScene(setupGame()) })

                Key.N9 -> sceneContainer.changeTo({ CourseBuilderScene() })
                Key.N0 -> {
                    when (val scene = sceneContainer.currentScene) {
                        is GameScene -> sceneContainer.changeTo({ CourseBuilderScene(scene.gameModel.course) })
                        is CourseBuilderScene -> sceneContainer.changeTo({ GameScene(setupGame(scene.course, 1)) })
                    }
                }

                else -> Unit
            }
        }
    }
}


