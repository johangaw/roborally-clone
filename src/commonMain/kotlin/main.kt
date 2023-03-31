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

    private lateinit var gameModel: GameModel
    private lateinit var robots: Map<RobotId, RobotView>
    private lateinit var programAreas: List<ProgramArea>
    private lateinit var bitmapCache: BitmapCache
    var fieldSize: Double = 0.0
    val indent = 2
    val cellSize: Double get() = (fieldSize - indent * 2) / 10.0

    override suspend fun SContainer.sceneMain() {
        bitmapCache = BitmapCache.create()
        fieldSize = min(views.virtualWidth - 10.0 * 2.0, views.virtualHeight - 200.0)
        gameModel = setupGame()

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

        val sprites = resourcesVfs["sprites.xml"].readAtlas()
        robots = gameModel.robots.mapIndexed {index, robot ->
            robot.id to robotView(robot.id, playerNumber(index), robot.dir, sprites, cellSize) {
                position(robotPosition(robot.pos))
            }
        }.toMap()

        programAreas = gameModel.players.map { player ->
            programArea(cellSize, gameModel.checkpoints.map { it.id }, player.id, player.robotId, bitmapCache) {
                alignTopToBottomOf(bgField)
                text(player.id.value.toString(), textSize = 30.0, color = Colors.BLACK) {
                    val textPadding = 10.0
                    alignTopToTopOf(parent!!, textPadding)
                    alignLeftToLeftOf(parent!!, textPadding)
                }
                visible = false

                dealCards(player.hand)
            }
        }
        programAreas.first().visible = true

        keys {
            down {
                when (it.key) {
                    Key.S -> {
                        val focusedProgrammingAreaIndex = programAreas.indexOfFirst { it.visible }
                        programAreas.forEach {
                            it.visible = false
                        }
                        programAreas[(focusedProgrammingAreaIndex + 1) % programAreas.size].visible = true
                    }

                    Key.F -> {
                        launchImmediately {
                            animate {
                                animateShowWinnerPopup(RoundResolution.WinnerResolution(gameModel.players.last().id))
                            }
                        }
                    }

                    Key.SPACE -> {
                        val cards = programAreas.associate { it.playerId to it.getSelectedCards() }
                        val result = gameModel.resolveRound(cards)
                        animateAllResults(result.resolutions)
                        gameModel = result.gameModel
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun Container.animateAllResults(resolutions: List<RoundResolution>) {
        launchImmediately {
            animate {
                sequence {
                    resolutions.forEach { resolution ->
                        animateResolution(resolution)
                        wait()
                    }
                }
            }
        }
    }

    private fun Animator.animateResolution(resolution: RoundResolution) {
        sequence(defaultTime = 500.milliseconds, defaultSpeed = 256.0) {
            when (resolution) {
                is RoundResolution.ActionCardResolution -> animateActionCard(resolution)
                is RoundResolution.CheckpointResolution -> animateCaptureCheckpoint(resolution)
                is RoundResolution.LaserResolution -> animateLasers(resolution)
                is RoundResolution.WinnerResolution -> animateShowWinnerPopup(resolution)
                is RoundResolution.WipeRegistersResolution -> animateWipeRegisters(resolution)
                is RoundResolution.DealCardsResolution -> animateDealActionCards(resolution)
            }
        }
    }

    private fun Animator.animateActionCard(
        resolution: RoundResolution.ActionCardResolution
    ) {
        resolution.steps.forEachIndexed { stepIndex, step ->
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
    }

    private fun Animator.animateDealActionCards(resolution: RoundResolution.DealCardsResolution) {
        block {
            resolution.hands.forEach { (playerId, hand) ->
                programAreas
                    .first { it.playerId == playerId }
                    .dealCards(hand)
            }
        }
    }

    private fun Animator.animateWipeRegisters(resolution: RoundResolution.WipeRegistersResolution) {
        block {
            programAreas.forEach { programmingArea ->
                programmingArea.clearCards()
                val lockedCards = resolution.lockedRegisters.getOrDefault(programmingArea.robotId, null)

                if(lockedCards != null) {
                    programmingArea.dealCards(lockedCards.map { register -> register.card })
                    lockedCards.forEach { register ->
                        programmingArea.lockRegister(register.index, register.card)
                    }
                }
            }
        }
    }

    private fun Animator.animateShowWinnerPopup(resolution: RoundResolution.WinnerResolution) {
        block {
            this@GameScene.sceneContainer.apply {
                val padding = 20.0
                roundRect(500.0, 300.0, 3.0,3.0) {
                    val text = text("Congraz player ${resolution.winner.value}") {
                        color = Colors.RED
                        textSize = 50.0
                    }
                    scaledWidth =  text.width + padding * 2
                    text.apply {
                        centerOn(parent!!)
                    }
                    centerOn(parent!!)
                }
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
                resolution.lockedRegisters.forEach { (robotId, lockedRegisters) ->
                    val programArea = programAreas.first { it.robotId == robotId }
                    lockedRegisters.forEach { programArea.lockRegister(it.index, it.card) }
                }
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
