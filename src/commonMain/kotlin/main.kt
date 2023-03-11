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
import com.soywiz.korma.geom.*
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

    var fieldSize: Double = 0.0
    val indent = 2
    val cellSize: Double get() = (fieldSize - indent * 2) / 10.0

    override suspend fun SContainer.sceneMain() {
        fieldSize = min(views.virtualWidth - 10.0 * 2.0, views.virtualHeight - 200.0)

        val playerOneRobot = Robot(Pos(4, 4), Direction.Down)
        val playerTwoRobot = Robot(Pos(4, 6), Direction.Right)

        var gameModel = GameModel(
            listOf(
                playerOneRobot,
                playerTwoRobot,
            ), listOf(
                Wall(Pos(2, 2), Direction.Left),
                Wall(Pos(2, 2), Direction.Right),
                Wall(Pos(2, 2), Direction.Up),
                Wall(Pos(2, 2), Direction.Down),
            ), listOf(
                Player(robotId = playerOneRobot.id),
                Player(robotId = playerTwoRobot.id),
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
                                    Direction.Up -> roundRect(
                                        cellSize * x, cellSize * y, cellSize, wallThickness, roundness
                                    )

                                    Direction.Down -> roundRect(
                                        cellSize * x,
                                        cellSize * (y + 1) - wallThickness,
                                        cellSize,
                                        wallThickness,
                                        roundness
                                    )

                                    Direction.Right -> roundRect(
                                        cellSize * (x + 1) - wallThickness,
                                        cellSize * y,
                                        wallThickness,
                                        cellSize,
                                        roundness
                                    )

                                    Direction.Left -> roundRect(
                                        cellSize * x, cellSize * y, wallThickness, cellSize, roundness
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val robots = gameModel.robots.map {
            val robotView = image(resourcesVfs["robot2.png"].readBitmap()) {
                position(robotPosition(it.pos))
                size(cellSize, cellSize)
            }
            it.id to robotView
        }.toMap()

        val programAreas = gameModel.players.map { player ->
            programArea(cellSize, player.id) {
                alignTopToBottomOf(bgField)
                text(player.id.value.toString(), textSize = 30.0, color = Colors.BLACK) {
                    val textPadding = 10.0
                    alignTopToTopOf(parent!!, textPadding)
                    alignLeftToLeftOf(parent!!, textPadding)
                }
            }
        }
        programAreas.first().zIndex = 1.0

        keys {
            down {
                when (it.key) {
                    Key.D -> {
                        val result = gameModel.dealActionCards()
                        gameModel = result.gameModel

                        result.hands.forEach {(playerId, hand) ->
                            programAreas.first { it.playerId == playerId }.dealCards(hand)
                        }
                    }

                    Key.S -> {
                        val focusedProgrammingAreaIndex = programAreas.indexOfFirst { it.zIndex > 0 }
                        programAreas.forEach {
                            it.zIndex = 0.0
                        }
                        programAreas[(focusedProgrammingAreaIndex + 1) % programAreas.size].zIndex = 1.0
                    }

                    Key.R -> {
                        val robotId = gameModel.robots.first().id
                        val actionCard = programAreas.first().selectedCards.first() ?: return@down
                        val result = gameModel.controlRobot(robotId, actionCard)

                        when (result) {
                            is RobotActionResult.Moved -> {
                                gameModel = result.gameModel
                                launchImmediately {
                                    animate {
                                        animateMovedResult(result, robots)
                                    }
                                }
                            }
                        }
                    }

                    Key.SPACE -> {
                        val robotId = gameModel.robots.first().id
                        val cards = programAreas.first().selectedCards.filterNotNull()

                        val results = gameModel.controlRobot(robotId, cards)

                        animateAllResults(results, robots)
                        gameModel = results.last().gameModel
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun Container.animateAllResults(results: List<RobotActionResult>, robots: Map<RobotId, View>) {
        launchImmediately {
            animate {
                sequence {
                    results.forEach { result ->
                        when (result) {
                            is RobotActionResult.Moved -> {
                                animateMovedResult(result, robots)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun robotPosition(pos: Pos): IPoint = IPoint(indent + pos.x * cellSize, indent + pos.y * cellSize)

    private fun Animator.animateMovedResult(result: RobotActionResult.Moved, robots: Map<RobotId, View>) {
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
                        val newPos = robotPosition(pos)
                        moveTo(
                            viewRobot, newPos.x, newPos.y, 0.5.seconds, easing
                        )
                    }
                }
            }
        }
    }
}


private fun GameModel.controlRobot(robotId: RobotId, cards: List<ActionCard>): List<RobotActionResult> =
    cards.runningFold<ActionCard, Pair<GameModel, RobotActionResult?>>(this to null) { (gameModel, _), card ->
        gameModel.controlRobot(robotId, card).let { result ->
            result.gameModel to result
        }
    }.mapNotNull { (_, result) -> result }

