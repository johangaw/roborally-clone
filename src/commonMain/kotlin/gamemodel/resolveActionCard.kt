package gamemodel

import com.soywiz.kds.*
import gamemodel.MovementPart.Move
import gamemodel.MovementPart.TakeCheckpoint
import java.lang.Integer.min
import kotlin.math.*

fun GameModel.resolveActionCard(id: RobotId, card: ActionCard): ActionCardResolutionResult {
    return when (card) {
        is ActionCard.MoveForward -> resolveActionCard(id, card)
        is ActionCard.Turn -> resolveActionCard(id, card)
    }
}

private fun GameModel.resolveActionCard(id: RobotId, card: ActionCard.MoveForward): ActionCardResolutionResult {
    val robot = getRobot(id)
    val pushDirection = if (card.distance > 0) robot.dir else robot.dir.opposite()

    return (1..abs(card.distance))
        .runningFold(MovementResolution(this, emptyList())) { acc, _ ->
            acc.gameModel.resolveMovement(id, pushDirection)
        }
        .drop(1)
        .filter { movementResolutions -> movementResolutions.parts.isNotEmpty() }
        .let { movementResolutions ->
            ActionCardResolutionResult(
                gameModel = movementResolutions.last().gameModel,
                steps = movementResolutions.map {
                    ActionCardResolutionStep.MovementStep(it.parts)
                })
        }
}

private fun GameModel.resolveMovement(robotId: RobotId, dir: Direction): MovementResolution {
    val robot = getRobot(robotId)
    val path = getPath(robot.pos, dir, 100 /* TODO up till end of board */).takeWhile { p ->
        wallAt(p, dir.opposite()) == null
    }
    val canMove = path.count { robotAt(it) == null } > 0

    if (!canMove) return MovementResolution(this, emptyList())

    val robotsToPush = path
        .takeWhile { robotAt(it) != null }
        .mapNotNull { robotAt(it) }
    val newPositions = (listOf(robot) + robotsToPush).associate { it.id to it.pos + dir }
    val completedCheckpoints = newPositions
        .filter { (id, pos) -> nextCheckpoint(id)?.pos == pos }
        .map { (id, _) -> getPlayer(id).id to nextCheckpoint(id)!!.id }
        .toMap()

    return MovementResolution(
        gameModel = this.copy(
            robots = robots.map { it.copy(pos = newPositions.getOrDefault(it.id, it.pos)) },
            players = players.map {
                if (it.id in completedCheckpoints) it.copy(
                    completedCheckpoints = it.completedCheckpoints + completedCheckpoints.getValue(it.id)
                )
                else it
            }),
        parts = newPositions.map { (id, pos) -> Move(id, pos) }
            + completedCheckpoints.map { (playerId, checkpointId) ->
            TakeCheckpoint(
                playerId, checkpointId
            )
        })
}

private data class MovementResolution(val gameModel: GameModel, val parts: List<MovementPart>)

private fun GameModel.resolveActionCard(robotId: RobotId, card: ActionCard.Turn): ActionCardResolutionResult {
    val robot = getRobot(robotId)
    val dir = robot.dir + card.type

    return ActionCardResolutionResult(
        gameModel = mapRobot(robotId) { it.copy(dir = dir) }, steps = listOf(
            ActionCardResolutionStep.TurningStep(
                robotId = robotId, newDirection = dir
            )
        )
    )
}

private operator fun Direction.plus(rotation: Turn): Direction = when (this) {
    Direction.Up -> when (rotation) {
        Turn.Right -> Direction.Right
        Turn.Left -> Direction.Left
        Turn.UTurn -> Direction.Down
    }

    Direction.Down -> when (rotation) {
        Turn.Right -> Direction.Left
        Turn.Left -> Direction.Right
        Turn.UTurn -> Direction.Up
    }

    Direction.Right -> when (rotation) {
        Turn.Right -> Direction.Down
        Turn.Left -> Direction.Up
        Turn.UTurn -> Direction.Left
    }

    Direction.Left -> when (rotation) {
        Turn.Right -> Direction.Up
        Turn.Left -> Direction.Down
        Turn.UTurn -> Direction.Right
    }
}


private fun getPath(pos: Pos, dir: Direction, distance: Int): List<Pos> =
    (1..distance).map { Pos(pos.x + dir.dx * it, pos.y + dir.dy * it) }

data class ActionCardResolutionResult(val gameModel: GameModel, val steps: List<ActionCardResolutionStep>)


sealed class ActionCardResolutionStep() {
    data class TurningStep(val robotId: RobotId, val newDirection: Direction) : ActionCardResolutionStep()

    data class MovementStep(val parts: List<MovementPart>) : ActionCardResolutionStep() {

        constructor(vararg parts: MovementPart) : this(parts.toList())
        constructor(vararg parts: Pair<RobotId, Pos>) : this(parts.map { (id, pos) -> Move(id, pos) })
    }
}

sealed class MovementPart {
    data class Move(val robotId: RobotId, val newPos: Pos) : MovementPart()
    data class TakeCheckpoint(val playerId: PlayerId, val checkpointId: CheckpointId) : MovementPart()
}
