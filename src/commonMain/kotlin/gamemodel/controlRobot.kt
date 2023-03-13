package gamemodel

import java.lang.Integer.min
import kotlin.math.*

fun GameModel.controlRobot(id: RobotId, card: ActionCard): RobotActionResult {
    return when (card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
        is ActionCard.Turn -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: RobotId, card: ActionCard.MoveForward): RobotActionResult {
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
            listOf(robot.id to robot.pos) // Start with robots original position
        ) { acc, pos ->
            (robotAt(pos)?.let { acc + (it.id to pos) } ?: acc)
                .map { (id, p) -> id to p + pushDirection }
        }.map { it.toMap() }
        .drop(1) // Remove robots original position

    val finalPositions = movingSteps.last()
    return RobotActionResult.Moved(
        copy(robots = robots.map { it.copy(pos = finalPositions.getOrDefault(it.id, it.pos)) }),
        movingSteps
    )
}

private fun GameModel.controlRobot(robotId: RobotId, card: ActionCard.Turn): RobotActionResult {
    val robot = getRobot(robotId)
    val dir = robot.dir + card.type

    return RobotActionResult.Turned(
        gameModel = mapRobot(robotId) { it.copy(dir = dir) },
        robotId = robotId,
        newDirection = dir
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

sealed class RobotActionResult {
    abstract val gameModel: GameModel

    data class Moved(override val gameModel: GameModel, val moveSteps: List<Map<RobotId, Pos>>) :
        RobotActionResult()

    data class Turned(override val gameModel: GameModel, val robotId: RobotId, val newDirection: Direction) :
        RobotActionResult()
}
