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
import gamemodel.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo({ GameScene() })
}


class GameScene : Scene() {
    override suspend fun SContainer.sceneMain() {

        val fieldSize = views.virtualWidth - 10.0 * 2.0
        val indent = 2
        val cellSize = (fieldSize - indent * 2) / 10.0

        var gameModel = GameModel(listOf(Robot(Pos(4, 4), Direction.Right)))

        val bgField = roundRect(fieldSize, fieldSize, 5.0, fill = Colors["#b9aea0"]) {
            graphics {
                it.position(indent, indent)
                repeat(10) { x ->
                    repeat(10) { y ->
                        fill(Colors["#cec0b2"]) {
                            roundRect(
                                cellSize * x + indent,
                                cellSize * y + indent,
                                cellSize - indent * 2,
                                cellSize - indent * 2,
                                5.0
                            )
                        }
                    }
                }
            }
        }

        val robots = gameModel.robots.map {
            val robotView = image(resourcesVfs["robot2.png"].readBitmap()) {
                position(indent + it.pos.x * cellSize, indent + it.pos.y * cellSize)
                size(cellSize, cellSize)
            }
            it.id to robotView
        }.toMap()

        keys {
            down {
                when (it.key) {
                    Key.SPACE -> {
                        val robotId = gameModel.robots.first().id
                        when (val result =
                            gameModel.controlRobot(robotId, ActionCard.MoveForward(2))) {
                            is RobotActionResult.Moved -> {
                                val viewRobot = robots.getValue(robotId)
                                viewRobot.x = indent + result.newPosition.x * cellSize
                                viewRobot.y = indent + result.newPosition.y * cellSize
                                gameModel = result.gameModel
                            }
                        }

                    }

                    else -> Unit
                }
            }
        }
    }
}
