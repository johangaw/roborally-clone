package gamemodel

fun GameModel.controlRobot(id: ResourceId, card: ActionCard): RobotActionResult {
    return when(card) {
        is ActionCard.MoveForward -> controlRobot(id, card)
    }
}

private fun GameModel.controlRobot(id: ResourceId, card: ActionCard.MoveForward): RobotActionResult {
    val robot = getRobot(id)
    val newPath = getPath(robot.pos, robot.dir, card.distance)
    val newPos = newPath.last()
    return RobotActionResult.Moved(
        mapRobot(id) { it.copy(pos = newPos) },
        newPos
    )
}

fun getPath(pos: Pos, dir: Direction, distance: Int): List<Pos> =
    (1..distance).map { Pos(pos.x + dir.dx * it, pos.y + dir.dy * it) }

sealed class RobotActionResult {
    data class Moved(val gameModel: GameModel, val newPosition: Pos): RobotActionResult()
}
