package gamemodel

import gamemodel.ActionCardResolutionStep.MovementStep.MovementPart
import java.lang.Integer.min
import kotlin.math.*

fun GameModel.controlRobot(id: RobotId, card: ActionCard): ActionCardResolutionResult {
    return when (card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
        is ActionCard.Turn -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: RobotId, card: ActionCard.MoveForward): ActionCardResolutionResult {
    val robot = getRobot(id)
    val pushDirection = if (card.distance > 0) robot.dir else robot.dir.opposite()

    val maxWantToMovePath = getPath(robot.pos, pushDirection, abs(card.distance))
    val maxFreePath = getPath(robot.pos, pushDirection, 100 /* TODO up till end of board */)
        .takeWhile { p -> wallAt(p, pushDirection.opposite()) == null }
    val robotsInFreePath = maxFreePath.count { robotAt(it) != null }
    val maxMovableDistance = min(maxFreePath.size - robotsInFreePath, maxWantToMovePath.size)
    val movablePath = getPath(robot.pos, pushDirection, maxMovableDistance)

    val movingSteps = movablePath
        .runningFold(
            listOf(MovementPart(robot.id, robot.pos)) // Start with robots original position
        ) { acc, pos ->
            (robotAt(pos)?.let { acc + MovementPart(it.id, pos) } ?: acc)
                .map { (id, p) -> MovementPart(robotId = id, newPos = p + pushDirection) }
        }
        .drop(1) // Remove robots original position

    val finalPositions = movingSteps.last().associate { (id, p) -> id to p }
    return ActionCardResolutionResult(
        gameModel = copy(robots = robots.map { it.copy(pos = finalPositions.getOrDefault(it.id, it.pos)) }),
        steps = movingSteps.map { ActionCardResolutionStep.MovementStep(it) }
    )
}

private fun GameModel.controlRobot(robotId: RobotId, card: ActionCard.Turn): ActionCardResolutionResult {
    val robot = getRobot(robotId)
    val dir = robot.dir + card.type

    return ActionCardResolutionResult(
        gameModel = mapRobot(robotId) { it.copy(dir = dir) },
        steps = listOf(
            ActionCardResolutionStep.TurningStep(
                robotId = robotId,
                newDirection = dir
            )
        )
    )
}

private operator fun Direction.plus(rotation: Turn): Direction =
    when (this) {
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
        constructor(vararg parts: Pair<RobotId, Pos>) : this(parts.map { (id, pos) -> MovementPart(id, pos) })

        data class MovementPart(val robotId: RobotId, val newPos: Pos)

    }

}
