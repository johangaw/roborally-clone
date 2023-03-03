package gamemodel

fun GameModel.controlRobot(id: RobotId, card: ActionCard): RobotActionResult {
    return when (card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: RobotId, card: ActionCard.MoveForward): RobotActionResult {
    val robot = getRobot(id)
    val pushDirection = robot.dir
    val path =
        getPath(robot.pos, pushDirection, card.distance).takeWhile { p -> wallAt(p, pushDirection.opposite()) == null }
    // TODO getClearPath

    // TODO make sure robot can be moved all along this path (nothing blocking her or pushed robots)

    val movingSteps = path.runningFold(listOf(robot.id to robot.pos)) { acc, pos ->
        val robotAtCurrent = robotAt(pos)
        (robotAtCurrent?.let { acc + (it.id to pos) } ?: acc)
            .map { (id, p) -> id to p + pushDirection }
    }.map { it.toMap() }
        .drop(1)

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
