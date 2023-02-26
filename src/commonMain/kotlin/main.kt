import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.roundRect
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.vector.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
	val sceneContainer = sceneContainer()
	sceneContainer.changeTo({ GameScene() })
}


class GameScene : Scene() {
    override suspend fun SContainer.sceneMain() {

        val fieldSize = views.virtualWidth - 10.0 * 2.0
        val indent = 2
        val cellSize = (fieldSize - indent * 2) / 10.0

        val bgField = roundRect(fieldSize, fieldSize, 5.0, fill = Colors["#b9aea0"]) {
            graphics {
                it.position(indent, indent)
                repeat(10) { x ->
                    repeat(10) { y ->
                        fill(Colors["#cec0b2"]) {
                            roundRect(cellSize * x + indent, cellSize * y + indent, cellSize - indent * 2, cellSize - indent * 2, 5.0)
                        }
                    }
                }
            }
        }

        var robotX = 4
        var robotY = 6
        val robot = image(resourcesVfs["robot2.png"].readBitmap()) {
            position(indent + robotX * cellSize, indent + robotY * cellSize)
            size(cellSize, cellSize)
        }

        keys {
            down {
                when(it.key) {
                    Key.LEFT -> robot.x -= cellSize
                    Key.RIGHT -> robot.x += cellSize
                    Key.UP -> robot.y -= cellSize
                    Key.DOWN -> robot.y += cellSize
                    else -> Unit
                }
            }
        }
    }
}
