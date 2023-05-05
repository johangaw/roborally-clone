package ui.scenes

import com.soywiz.klock.milliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.IPoint
import com.soywiz.korma.geom.Point
import com.soywiz.korma.interpolation.Easing
import gamemodel.*
import ui.*
import ui.animations.laserAlpha
import ui.animations.laserBurn
import ui.animations.shake
import kotlin.math.min

class GameScene(var gameModel: GameModel) : Scene() {
    private lateinit var robots: Map<RobotId, RobotView>
    private lateinit var programAreas: List<ProgramArea>
    private lateinit var courseView: CourseView
    private lateinit var bitmapCache: BitmapCache

    private val courseZIndex = 0.0
    private val programAreaZIndex = 0.0
    private val robotZIndex = 10.0
    private val laserZIndex = 9.0

    override suspend fun SContainer.sceneInit() {
        bitmapCache = BitmapCache.create()
        courseView = courseView(gameModel.course, bitmapCache, showStartPositions = false) {
            val programmingAreaHeight = 200.0
            val scaleFactor =
                min(views.virtualWidthDouble / width, (views.virtualHeightDouble - programmingAreaHeight) / height)
            scale = scaleFactor
            zIndex = courseZIndex
            centerOn(this@sceneInit)
            alignTopToTopOf(this@sceneInit)
        }
        val cellSize = courseView.cellSize

        val sprites = resourcesVfs["sprites.xml"].readAtlas()
        robots = gameModel.robots
            .mapIndexed { index, robot ->
                robot.id to robotView(playerNumber(index), robot.dir, sprites) {
                    setSizeScaled(cellSize, cellSize)
                    position(robotPosition(robot.pos))
                    zIndex = robotZIndex
                }
            }
            .toMap()

        programAreas = gameModel.players.map { player ->
            programArea(gameModel.course.checkpoints.map { it.id }, player.id, player.robotId, bitmapCache) {
                zIndex = programAreaZIndex
                centerOn(this@sceneInit)
                alignTopToBottomOf(courseView)
                text(player.id.value.toString(), textSize = 30.0, color = Colors.BLACK) {
                    val textPadding = 10.0
                    alignBottomToBottomOf(parent!!, textPadding)
                    alignRightToRightOf(parent!!, textPadding)
                }
                visible = false

                dealCards(player.hand)
            }
        }
        programAreas.first().visible = true

        keys {
            down {
                when (it.key) {
                    Key.ESCAPE -> {
                        println(serialize(gameModel))
                    }

                    Key.S -> {
                        val focusedProgrammingAreaIndex = programAreas.indexOfFirst { area -> area.visible }
                        programAreas.forEach { area -> area.visible = false }
                        programAreas[(focusedProgrammingAreaIndex + 1) % programAreas.size].visible = true
                    }

                    Key.F -> {
                        val result = gameModel.resolveLasers()
                        animate {
                            animateLasers(
                                RoundResolution.LaserResolution(
                                    result.laserPaths, result.remainingHealthOfDamagedRobots
                                )
                            )
                        }
                    }

                    Key.SPACE -> {
                        val cards = programAreas.associate { area -> area.playerId to area.getSelectedCards() }
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
            pos.y * courseView.cellSize + courseView.y + basePoint.y
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
                is RoundResolution.ActionCardMovementResolution -> animateMovement(resolution)
                is RoundResolution.ActionCardRotationResolution -> animateRotation(resolution)
                is RoundResolution.ConveyorBeltsResolution -> animateConveyorBelts(resolution)
                is RoundResolution.CheckpointResolution -> animateCaptureCheckpoint(resolution)
                is RoundResolution.LaserResolution -> animateLasers(resolution)
                is RoundResolution.WinnerResolution -> animateShowWinnerPopup(resolution)
                is RoundResolution.SpawnedRobotsResolution -> animateSpawnRobots(resolution)
                is RoundResolution.WipeRegistersResolution -> animateWipeRegisters(resolution)
                is RoundResolution.DealCardsResolution -> animateDealActionCards(resolution)
                is RoundResolution.RegisterLockingResolution -> animateRegisterLocking(resolution)
            }
        }
    }

    private fun Animator.animateRegisterLocking(resolution: RoundResolution.RegisterLockingResolution) {
        block {
            resolution.lockedRegisters.forEach { (robotId, lockedRegisters) ->
                val programArea = programAreas.first { it.robotId == robotId }
                lockedRegisters.forEach { programArea.lockRegister(it.index, it.card) }
            }
        }
    }

    private fun Animator.animateSpawnRobots(resolution: RoundResolution.SpawnedRobotsResolution) {
        parallel {
            resolution.spawnedRobots.forEach {
                val viewRobot = robots.getValue(it.id)
                block {
                    viewRobot.direction = it.dir
                    viewRobot.position(robotPosition(it.pos))
                }
                viewRobot.respawn(this)
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
                if (resolution.movedRobots.isNotEmpty()) {
                    courseView.conveyorBelts
                        .filter { it.key in resolution.activatedPositions }
                        .values
                        .forEach {
                            shake(
                                it,
                                500.milliseconds
                            )
                        }
                }
            }
            parallel {
                resolution.remainingHealthOfFallenRobots.keys.forEach { robotId ->
                    val robotView = robots.getValue(robotId)
                    robotView.destroy(this)
                }
                block {
                    resolution.remainingHealthOfFallenRobots.forEach { (robotId, health) ->
                        val programArea = programAreas.first { it.robotId == robotId }
                        programArea.setHealth(health)
                    }
                }
            }
            parallel {
                resolution.rotatedRobots.forEach { (id, dir) ->
                    val viewRobot = robots.getValue(id)
                    block {
                        viewRobot.direction = dir
                    }
                }
                if (resolution.rotatedRobots.isNotEmpty()) courseView.conveyorBelts.values.forEach {
                    shake(
                        it,
                        500.milliseconds
                    )
                }
            }
        }
    }

    private fun Animator.animateMovement(
        resolution: RoundResolution.ActionCardMovementResolution,
    ) {
        resolution.steps.forEachIndexed { stepIndex, step ->
            val easing = when (stepIndex) {
                0 -> Easing.EASE_IN
                resolution.steps.lastIndex -> Easing.EASE_OUT
                else -> Easing.LINEAR
            }

            parallel {
                step.moves.forEach { move ->
                    val viewRobot = robots.getValue(move.robotId)
                    val newPos = robotPosition(move.newPos)
                    moveTo(viewRobot, newPos.x, newPos.y, easing = easing)
                    block {
                        viewRobot.playAnimation(500.milliseconds)
                    }
                }
            }

            parallel {
                step.falls.forEach { fall ->
                    val viewRobot = robots.getValue(fall.robotId)
                    val programArea = programAreas.first { it.robotId == fall.robotId }
                    viewRobot.destroy(this)
                    block {
                        programArea.setHealth(fall.remainingHealth)
                    }
                }
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
            this@GameScene.sceneView.apply {
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
        val beams = resolution.laserPaths
            .map {
                LaserBeamView(courseView.cellSize, it.path.size, it.dir, it.power, it.source).apply {
                    alpha = 0.0
                    zIndex = laserZIndex
                    addTo(this@GameScene.sceneView)
                    position(robotPosition(it.path.first(), pos))
                }
            }
        val damagedRobots = resolution.remainingHealthOfDamagedRobots.keys.map { robots.getValue(it) }
        val time = 500.milliseconds
        sequence {
            parallel(time) {
                beams.forEach { laserAlpha(it, time) }
                damagedRobots.forEach { laserBurn(it, time) }
                block {
                    resolution.remainingHealthOfDamagedRobots.forEach { (id, remainingHealth) ->
                        programAreas
                            .first { it.robotId == id }
                            .setHealth(remainingHealth)
                    }
                }
            }
            block {
                beams.forEach { it.removeFromParent() }
            }
        }
    }

    private fun Animator.animateRotation(
        resolution: RoundResolution.ActionCardRotationResolution,
    ) = animateRotation(resolution.robotId, resolution.newDirection)

    private fun Animator.animateRotation(robotId: RobotId, newDirection: Direction) {
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
}
