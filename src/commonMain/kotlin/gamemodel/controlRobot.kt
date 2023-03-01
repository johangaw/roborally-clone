package gamemodel

fun GameModel.controlRobot(id: ResourceId, card: ActionCard): RobotActionResult {
    return when (card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: ResourceId, card: ActionCard.MoveForward): RobotActionResult {
    val robot = getRobot(id)
    val pushDirection = robot.dir
    val path = getPath(robot.pos, pushDirection, card.distance)
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

private operator fun Pos.plus(dir: Direction): Pos = Pos(x + dir.dx, y + dir.dy)

fun getPath(pos: Pos, dir: Direction, distance: Int): List<Pos> =
    (1..distance).map { Pos(pos.x + dir.dx * it, pos.y + dir.dy * it) }

sealed class RobotActionResult {
    data class Moved(val gameModel: GameModel, val moveSteps: List<Map<ResourceId, Pos>>) :
        RobotActionResult()
}
