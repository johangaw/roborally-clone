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
import com.soywiz.korma.interpolation.*
import gamemodel.*
import ui.*
import kotlin.math.*

suspend fun main() = Korge(width = 1024, height = 1024, bgcolor = Colors["#2b2b2b"]) {
    val sceneContainer = sceneContainer()
    sceneContainer.changeTo({ GameScene() })
//    sceneContainer.changeTo({ CourseBuilderScene() })
}


class GameScene : Scene() {

    private lateinit var gameModel: GameModel
    private lateinit var robots: Map<RobotId, RobotView>
    private lateinit var programAreas: List<ProgramArea>
    private lateinit var courseView: CourseView
    private lateinit var bitmapCache: BitmapCache

    override suspend fun SContainer.sceneMain() {
        bitmapCache = BitmapCache.create()
        gameModel = setupGame(PreBuildCourses.Course1, playerCount = 1)

        courseView = courseView(gameModel.course, bitmapCache, showStartPositions = false) {
            val programmingAreaHeight = 200.0
            val scaleFactor =
                min(views.virtualWidthDouble / width, (views.virtualHeightDouble - programmingAreaHeight) / height)
            scale = scaleFactor
            centerOn(this@sceneMain)
            alignTopToTopOf(this@sceneMain)
        }
        val cellSize = courseView.cellSize

        val sprites = resourcesVfs["sprites.xml"].readAtlas()
        robots = gameModel.robots
            .mapIndexed { index, robot ->
                robot.id to robotView(playerNumber(index), robot.dir, sprites) {
                    setSizeScaled(cellSize, cellSize)
                    position(robotPosition(robot.pos))
                }
            }
            .toMap()

        programAreas = gameModel.players.map { player ->
            programArea(cellSize, gameModel.course.checkpoints.map { it.id }, player.id, player.robotId, bitmapCache) {
                centerOn(this@sceneMain)
                alignTopToBottomOf(courseView)
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
                    Key.N0 -> {
                        sceneContainer.changeTo({ CourseBuilderScene() })
                    }

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
                                val view = courseView.conveyorBelts.values.first()
                                shake(view, 500.milliseconds)
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

    private fun robotPosition(pos: Pos, basePoint: IPoint = Point(0, 0)): IPoint {
        return IPoint(
            pos.x * courseView.cellSize + courseView.x + basePoint.x,
            pos.y * courseView.cellSize + courseView.y + +basePoint.y
        )
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
                is RoundResolution.ConveyorBeltsResolution -> animateConveyorBelts(resolution)
                is RoundResolution.CheckpointResolution -> animateCaptureCheckpoint(resolution)
                is RoundResolution.LaserResolution -> animateLasers(resolution)
                is RoundResolution.WinnerResolution -> animateShowWinnerPopup(resolution)
                is RoundResolution.WipeRegistersResolution -> animateWipeRegisters(resolution)
                is RoundResolution.DealCardsResolution -> animateDealActionCards(resolution)
            }
        }
    }

    private fun Animator.animateConveyorBelts(resolution: RoundResolution.ConveyorBeltsResolution) {
        sequence {
            parallel {
                resolution.movedRobots.forEach { (id, pos) ->
                    val viewRobot = robots.getValue(id)
                    val newPos = robotPosition(pos)
                    moveTo(viewRobot, newPos.x, newPos.y, easing = Easing.SMOOTH)
                }
                if (resolution.movedRobots.isNotEmpty())
                    courseView.conveyorBelts.values.forEach { shake(it, 500.milliseconds) }
            }
            parallel {
                resolution.rotatedRobots.forEach { (id, dir) ->
                    val viewRobot = robots.getValue(id)
                    block {
                        viewRobot.direction = dir
                    }
                }
                if (resolution.rotatedRobots.isNotEmpty())
                    courseView.conveyorBelts.values.forEach { shake(it, 500.milliseconds) }
            }
        }
    }

    private fun Animator.animateActionCard(
        resolution: RoundResolution.ActionCardResolution,
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

                if (lockedCards != null) {
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
                roundRect(500.0, 300.0, 3.0, 3.0) {
                    val text = text("Congraz player ${resolution.winner.value}") {
                        color = Colors.RED
                        textSize = 50.0
                    }
                    scaledWidth = text.width + padding * 2
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
            LaserBeam(courseView.cellSize, it.path.size, it.dir).apply {
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
                        block {
                            viewRobot.playAnimation(500.milliseconds)
                        }
                    }
                }
            }
        }
    }
}

