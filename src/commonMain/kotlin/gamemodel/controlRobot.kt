package gamemodel

import java.lang.Integer.min

fun GameModel.controlRobot(id: RobotId, card: ActionCard): RobotActionResult {
    return when (card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: RobotId, card: ActionCard.MoveForward): RobotActionResult {
    val robot = getRobot(id)
    val pushDirection = robot.dir

    val maxWantToMovePath = getPath(robot.pos, pushDirection, card.distance)
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

fun getPath(pos: Pos, dir: Direction, distance: Int): List<Pos> =
    (1..distance).map { Pos(pos.x + dir.dx * it, pos.y + dir.dy * it) }

sealed class RobotActionResult {
    data class Moved(val gameModel: GameModel, val moveSteps: List<Map<RobotId, Pos>>) :
        RobotActionResult()
}
