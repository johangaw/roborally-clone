package gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class Pos(val x: Int, val y: Int)

@Serializable
enum class Direction(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Right(1, 0), Left(-1, 0);
}

operator fun Pos.plus(dir: Direction): Pos = Pos(x + dir.dx, y + dir.dy)

fun Direction.opposite(): Direction = when (this) {
    Direction.Up -> Direction.Down
    Direction.Down -> Direction.Up
    Direction.Right -> Direction.Left
    Direction.Left -> Direction.Right
}

fun Direction.quoter(): Direction = when (this) {
    Direction.Up -> Direction.Right
    Direction.Down -> Direction.Left
    Direction.Right -> Direction.Down
    Direction.Left -> Direction.Up
}

data class Robot(
    val pos: Pos,
    val dir: Direction,
    val health: Int = 10,
    val registers: Set<Register> = emptySet(),
    val id: RobotId = RobotId.create(),
)

fun Set<Register>.mapToSet(transform: (Register) -> Register): Set<Register> = map(transform).toSet()

data class Register(
    val card: ActionCard,
    val index: Int,
    val locked: Boolean = false,
): Comparable<Register> {
    override fun compareTo(other: Register): Int =
        index.compareTo(other.index)
}

fun Collection<Register>.locked() = this.filter { it.locked }

fun Collection<Register>.unlocked() = this.filter { !it.locked }

fun Collection<Register>.cards() = this.map { it.card }


data class Player(
    val robotId: RobotId,
    val hand: List<ActionCard> = emptyList(),
    val capturedCheckpoints: List<CheckpointId> = emptyList(),
    val id: PlayerId = PlayerId.create(),
)

data class GameModel(
    val course: Course,
    val robots: List<Robot>,
    val players: List<Player>,
    val actionDrawPile: List<ActionCard> = actionCardDeck().shuffled(),
    val actionDiscardPile: List<ActionCard> = emptyList(),
    val destroyedRobots : List<Robot> = emptyList()
) {
    init {
        assertNoDoubletCards()
    }

    fun robotAt(pos: Pos): Robot? = robots.firstOrNull { it.pos == pos }

    fun mapRobot(id: RobotId, mapper: (robot: Robot) -> Robot): GameModel =
        copy(robots = robots.map { if (it.id == id) mapper(it) else it })

    fun getRobot(id: RobotId): Robot =
        robots.firstOrNull { it.id == id } ?: throw AssertionError("No robot with id $id")

    fun getRobot(id: PlayerId): Robot = getRobot(getPlayer(id).robotId)

    fun getPlayer(robotId: RobotId): Player =
        players.firstOrNull { it.robotId == robotId } ?: throw AssertionError("No player with robot $robotId")

    fun getPlayer(id: PlayerId): Player =
        players.firstOrNull { it.id == id } ?: throw AssertionError("No player with id $id")

    fun wallAt(pos: Pos, dir: Direction): Wall? = course.wallAt(pos, dir)

    fun isDestroyed(robotId: RobotId): Boolean = destroyedRobots.firstOrNull { it.id == robotId } != null

    private fun assertNoDoubletCards() {
        val allCards =
            actionDrawPile + actionDiscardPile + players.flatMap { it.hand } + robots.flatMap { it.registers.map { reg -> reg.card } }

        assert(allCards.size == allCards.toSet().size) {
            "some card(s) have some how ended up in more than one place..."
        }
    }
}
