import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import ui.scenes.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo({ GameScene() })
//    sceneContainer.changeTo({ CourseBuilderScene() })
}


