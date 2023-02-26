package gamemodel



private var freeId = 0
fun newId() = freeId++

fun GameModel.addRobot(pos: Pos): NewRobotResult {
    if(robotAt(pos) != null) return NewRobotResult.PositionAlreadyTaken

    val robot = Robot(pos, Direction.values().random())
    return NewRobotResult.RobotAdded(
        copy(robots = robots + robot),
        robot
    )
}

sealed class NewRobotResult {
    object PositionAlreadyTaken: NewRobotResult()
    data class RobotAdded(val gameModel: GameModel, val robot: Robot): NewRobotResult()
}
