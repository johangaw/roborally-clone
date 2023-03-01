import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.roundRect
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.interpolation.*
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

        var gameModel = GameModel(listOf(
            Robot(Pos(4, 4), Direction.Down),
            Robot(Pos(4, 6), Direction.Right),
        ))

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
                                gameModel = result.gameModel
                                launchImmediately {
                                    animate {
                                        sequence(defaultTime = 1.seconds, defaultSpeed = 256.0) {
                                            result.moveSteps.forEachIndexed { stepIndex, movements ->
                                                val easing = when(stepIndex) {
                                                    0 -> Easing.EASE_IN
                                                    result.moveSteps.lastIndex -> Easing.EASE_OUT
                                                    else -> Easing.LINEAR
                                                }
                                                parallel {
                                                    movements.forEach { (id, pos) ->
                                                        val viewRobot = robots.getValue(id)
                                                        moveTo(
                                                            viewRobot, indent + pos.x * cellSize,
                                                            indent + pos.y * cellSize,
                                                            0.5.seconds,
                                                            easing
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
