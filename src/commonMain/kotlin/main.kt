import com.soywiz.klock.*
import com.soywiz.klock.min
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
import ui.*
import kotlin.math.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo({ GameScene() })
}


class GameScene : Scene() {
    override suspend fun SContainer.sceneMain() {

        val fieldSize = min(views.virtualWidth - 10.0 * 2.0, views.virtualHeight - 200.0)
        val indent = 2
        val cellSize = (fieldSize - indent * 2) / 10.0

        var gameModel = GameModel(
            listOf(
                Robot(Pos(4, 4), Direction.Down),
                Robot(Pos(4, 6), Direction.Right),
            ),
            listOf(
                Wall(Pos(2,2), Direction.Left),
                Wall(Pos(2,2), Direction.Right),
                Wall(Pos(2,2), Direction.Up),
                Wall(Pos(2,2), Direction.Down),
            )
        )

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
                        val wallThickness = 10.0
                        val roundness = 3.0
                        fill(Colors.YELLOW) {
                            gameModel.wallsAt(Pos(x, y)).forEach { wall ->
                                when (wall.dir) {
                                    Direction.Up -> roundRect(cellSize * x, cellSize * y, cellSize, wallThickness, roundness)
                                    Direction.Down -> roundRect(cellSize * x, cellSize * (y + 1) - wallThickness, cellSize, wallThickness, roundness)
                                    Direction.Right -> roundRect(cellSize * (x+1)-wallThickness, cellSize * y, wallThickness, cellSize, roundness)
                                    Direction.Left -> roundRect(cellSize * x, cellSize * y, wallThickness, cellSize, roundness)
                                }
                            }
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

        val programArea = programArea(cellSize) {
            alignTopToBottomOf(bgField)
        }

        val cards = (-5..8).map { ActionCard.MoveForward(it) }
        programArea.dealCards(cards)



        keys {
            down {
                when (it.key) {
                    Key.SPACE -> {
                        val robotId = gameModel.robots.first().id
                        val actionCard = programArea.selectedCards.first() ?: return@down

                        when (val result =
                            gameModel.controlRobot(robotId, actionCard)) {
                            is RobotActionResult.Moved -> {
                                gameModel = result.gameModel
                                launchImmediately {
                                    animate {
                                        sequence(defaultTime = 1.seconds, defaultSpeed = 256.0) {
                                            result.moveSteps.forEachIndexed { stepIndex, movements ->
                                                val easing = when (stepIndex) {
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
