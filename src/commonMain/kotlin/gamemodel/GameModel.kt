package gamemodel

data class Pos(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    Up(0, -1),
    Down(0, 1),
    Right(1, 0),
    Left(-1, 0)
}

sealed class ActionCard {
    data class MoveForward(val distance: Int) : ActionCard()
}

data class Robot(val pos: Pos, val dir: Direction, val id: RobotId = RobotId.create())

data class Wall(val index: Int, val id: WallId = WallId.create())

data class GameModel(val robots: List<Robot>, val walls: List<Wall>) {
    fun robotAt(pos: Pos): Robot? = robots.firstOrNull { it.pos == pos }

    fun mapRobot(id: RobotId, mapper: (robot: Robot) -> Robot): GameModel =
        copy(robots = robots.map { if (it.id == id) mapper(it) else it })

    fun getRobot(id: RobotId): Robot =
        robots.firstOrNull { it.id == id } ?: throw AssertionError("No robot with id $id")
}






