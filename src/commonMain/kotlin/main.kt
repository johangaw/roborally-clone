import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.roundRect
import com.soywiz.korim.atlas.*
import com.soywiz.korim.color.*
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

    private lateinit var robots: Map<RobotId, RobotView>
    private lateinit var programAreas: List<ProgramArea>
    private lateinit var bitmapCache: BitmapCache
    var fieldSize: Double = 0.0
    val indent = 2
    val cellSize: Double get() = (fieldSize - indent * 2) / 10.0

    override suspend fun SContainer.sceneMain() {
        bitmapCache = BitmapCache.create()
        fieldSize = min(views.virtualWidth - 10.0 * 2.0, views.virtualHeight - 200.0)

        val playerOneRobot = Robot(Pos(4, 4), Direction.Down)
        val playerTwoRobot = Robot(Pos(4, 6), Direction.Right)

        var gameModel = GameModel(
            robots = listOf(
                playerOneRobot,
                playerTwoRobot,
            ),
            walls = listOf(
                Wall(Pos(2, 2), Direction.Left),
                Wall(Pos(2, 2), Direction.Right),
                Wall(Pos(2, 2), Direction.Up),
                Wall(Pos(2, 2), Direction.Down),
            ),
            players = listOf(
                Player(robotId = playerOneRobot.id),
                Player(robotId = playerTwoRobot.id),
            ),
            checkpoints = listOf(
                Checkpoint(0, Pos(5, 6)),
                Checkpoint(1, Pos(2, 4)),
                Checkpoint(2, Pos(9, 9)),
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
                            gameModel
                                .wallsAt(Pos(x, y))
                                .forEach { wall ->
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

        gameModel.checkpoints.forEach {
            fixedSizeContainer(cellSize, cellSize) {
                position(robotPosition(it.pos))

                val image = image(bitmapCache.checkpoint) {
                    val checkpointSize = cellSize * 0.8
                    size(checkpointSize, checkpointSize)
                    centerOn(parent!!)
                }

                text(it.order.toString()) {
                    fontSize = 25.0
                    color = Colors.WHITE
                    centerOn(image)
                    alignTopToTopOf(image, cellSize * 0.1)
                }
            }
        }

        /**
         * X altas formula
         * width 46
         * gap 5
         *
         * Y atlas formula
         * height 65
         * gap 7
         */
        val sprites = resourcesVfs["sprites.xml"].readAtlas()
        robots = gameModel.robots.associate {
            it.id to robotView(it.id, it.dir, sprites, cellSize) {
                position(robotPosition(it.pos))
            }
        }

        programAreas = gameModel.players.map { player ->
            programArea(cellSize, gameModel.checkpoints.map { it.id }, player.id, bitmapCache) {
                alignTopToBottomOf(bgField)
                text(player.id.value.toString(), textSize = 30.0, color = Colors.BLACK) {
                    val textPadding = 10.0
                    alignTopToTopOf(parent!!, textPadding)
                    alignLeftToLeftOf(parent!!, textPadding)
                }
                visible = false
            }
        }
        programAreas.first().visible = true

        keys {
            down {
                when (it.key) {
                    Key.D -> {
                        val result = gameModel.dealActionCards()
                        gameModel = result.gameModel

                        result.hands.forEach { (playerId, hand) ->
                            programAreas
                                .first { it.playerId == playerId }
                                .dealCards(hand)
                        }
                    }

                    Key.S -> {
                        val focusedProgrammingAreaIndex = programAreas.indexOfFirst { it.visible }
                        programAreas.forEach {
                            it.visible = false
                        }
                        programAreas[(focusedProgrammingAreaIndex + 1) % programAreas.size].visible = true
                    }

                    Key.F -> {
                        animate {
                            sequence {
                                animateLasers(
                                    RoundResolution.LaserResolution(
                                        setOf(
                                            LaserPath(
                                                listOf(
                                                    Pos(5, 6),
                                                    Pos(6, 6),
                                                    Pos(7, 6),
                                                    Pos(8, 6),
                                                    Pos(9, 6),
                                                ),
                                                LaserDirection.Right
                                            ),
                                            LaserPath(
                                                listOf(Pos(4, 5), Pos(4, 6)),
                                                LaserDirection.Down
                                            )
                                        ),
                                        mapOf(playerTwoRobot.id to 1)
                                    )
                                )
                            }
                        }

                    }

                    Key.SPACE -> {
                        val cards = programAreas.associate { it.playerId to it.getSelectedCards() }
                        val result = gameModel.resolveRound(cards)
                        animateAllResults(result.resolutions, robots)
                        gameModel = result.gameModel
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun Container.animateAllResults(resolutions: List<RoundResolution>, robots: Map<RobotId, RobotView>) {
        launchImmediately {
            animate {
                sequence {
                    resolutions.forEach { resolution ->
                        animateResolution(resolution, robots)
                        wait()
                    }
                }
            }
        }
    }

    private fun Animator.animateResolution(resolution: RoundResolution, robots: Map<RobotId, RobotView>) {
        sequence(defaultTime = 500.milliseconds, defaultSpeed = 256.0) {
            when (resolution) {
                is RoundResolution.ActionCardResolution -> resolution.steps.forEachIndexed { stepIndex, step ->
                    when (step) {
                        is ActionCardResolutionStep.MovementStep -> {
                            val easing = when (stepIndex) {
                                0 -> Easing.EASE_IN
                                resolution.steps.lastIndex -> Easing.EASE_OUT
                                else -> Easing.LINEAR
                            }
                            animateMovementParts(step.parts, easing, robots)
                        }

                        is ActionCardResolutionStep.TurningStep -> animateTurn(step.robotId, step.newDirection, robots)
                    }

                }

                is RoundResolution.CheckpointResolution -> animateCaptureCheckpoint(resolution)
                is RoundResolution.LaserResolution -> animateLasers(resolution)
            }
        }
    }

    private fun Animator.animateLasers(resolution: RoundResolution.LaserResolution) {
        val beams = resolution.laserPaths.map {
            LaserBeam(cellSize, it.path.size, it.dir).apply {
                alpha = 0.0
                addTo(this@GameScene.sceneContainer)
                position(robotPosition(it.path.first(), pos))
            }
        }
        val damagedRobots = resolution.damage.keys.map { robots.getValue(it) }
        sequence {
            parallel(time = 100.milliseconds) {
                beams.forEach { alpha(it, 1.0) }
                damagedRobots.forEach { tween(it::burning[1.0]) }
            }
            parallel(time = 100.milliseconds) {
                beams.forEach { alpha(it, 0.0) }
                damagedRobots.forEach { tween(it::burning[0.0]) }
            }
            parallel(time = 500.milliseconds) {
                beams.forEach { alpha(it, 1.0) }
                damagedRobots.forEach { tween(it::burning[1.0]) }
            }
            wait(time = 500.milliseconds)
            parallel(time = 500.milliseconds) {
                beams.forEach { alpha(it, 0.0) }
                damagedRobots.forEach { tween(it::burning[0.0]) }
            }
            block {
                beams.forEach { it.removeFromParent() }
            }
        }
    }

    private fun Animator.animateTurn(robotId: RobotId, newDirection: Direction, robots: Map<RobotId, RobotView>) {
        val robot = robots.getValue(robotId)
        sequence(defaultTime = 250.milliseconds) {
            moveBy(robot, -8.0, 0.0)
            moveBy(robot, 16.0, 0.0)
            moveBy(robot, -8.0, 0.0)
            wait()
            block {
                robot.direction = newDirection
            }
            wait()
        }
    }

    private fun robotPosition(pos: Pos, basePoint: IPoint = Point(0, 0)): IPoint =
        IPoint(indent + pos.x * cellSize + basePoint.x, indent + pos.y * cellSize + +basePoint.y)

    private fun Animator.animateCaptureCheckpoint(resolution: RoundResolution.CheckpointResolution) {
        parallel {
            resolution.capturedCheckpoints.forEach { (playerId, checkpointId) ->
                val programArea = programAreas.first { it.playerId == playerId }
                sequence(defaultTime = 500.milliseconds) {
                    block { programArea.markCheckpoint(checkpointId, true) }
                    wait()
                    block { programArea.markCheckpoint(checkpointId, false) }
                    wait()
                    block { programArea.markCheckpoint(checkpointId, true) }
                }
            }
        }
    }

    private fun Animator.animateMovementParts(
        parts: List<MovementPart>,
        easing: Easing,
        robots: Map<RobotId, RobotView>,
    ) {
        parallel {
            parts.forEach { part ->
                when (part) {
                    is MovementPart.Move -> {
                        val viewRobot = robots.getValue(part.robotId)
                        val newPos = robotPosition(part.newPos)
                        moveTo(viewRobot, newPos.x, newPos.y, easing = easing)
                    }
                }
            }
        }
    }
}
