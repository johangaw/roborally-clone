package gamemodel

data class Pos(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    Up(0, -1),
    Down(0, 1),
    Right(1, 0),
    Left(-1, 0);
}

operator fun Pos.plus(dir: Direction): Pos = Pos(x + dir.dx, y + dir.dy)

fun Direction.opposite(): Direction = when (this) {
    Direction.Up -> Direction.Down
    Direction.Down -> Direction.Up
    Direction.Right -> Direction.Left
    Direction.Left -> Direction.Right
}

sealed class ActionCard {
    data class MoveForward(val distance: Int) : ActionCard()
}

data class Robot(val pos: Pos, val dir: Direction, val id: RobotId = RobotId.create())

data class Wall(val pos: Pos, val dir: Direction, val id: WallId = WallId.create())

data class GameModel(val robots: List<Robot>, val walls: List<Wall>) {
    fun robotAt(pos: Pos): Robot? = robots.firstOrNull { it.pos == pos }

    fun mapRobot(id: RobotId, mapper: (robot: Robot) -> Robot): GameModel =
        copy(robots = robots.map { if (it.id == id) mapper(it) else it })

    fun getRobot(id: RobotId): Robot =
        robots.firstOrNull { it.id == id } ?: throw AssertionError("No robot with id $id")

    fun wallAt(pos: Pos, dir: Direction): Wall? {
        return walls.firstOrNull { it.pos == pos && it.dir == dir }
            ?: walls.firstOrNull { it.pos == pos + dir && it.dir == dir.opposite() }
    }
}






