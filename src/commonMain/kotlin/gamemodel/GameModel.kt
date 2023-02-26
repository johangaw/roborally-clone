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

@JvmInline
value class ResourceId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = ResourceId(freeId++)
    }
}

data class Robot(val pos: Pos, val dir: Direction, val id: ResourceId = ResourceId.create())

data class GameModel(val robots: List<Robot>) {
    fun robotAt(pos: Pos): Robot? = robots.firstOrNull { it.pos == pos }

    fun mapRobot(id: ResourceId, mapper: (robot: Robot) -> Robot): GameModel =
        copy(robots = robots.map { if (it.id == id) mapper(it) else it })

    fun getRobot(id: ResourceId): Robot =
        robots.firstOrNull { it.id == id } ?: throw AssertionError("No robot with id $id")
}






