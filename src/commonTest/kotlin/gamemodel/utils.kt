package gamemodel

import com.soywiz.kmem.*

/**
 * Full map (not yet implemented)
 *   +|+|+|+|+|+
 *   -         -
 *   +     →|  +
 *   -    ---  -
 *   +         +
 *   -         -
 *   +|+|+|+|+|+
 *
 * Simple map
 *   +|+|+|+|+|+
 *   +     →|  +
 *
 */
fun gameModel(map: String): GameModel {
    var robotIds = 0
    var wallIds = 0
    var playerId = 0
    var checkpointId = 0
    assertValidMap(map)

    val mapBody = map
        .split("\n")
        .drop(1)
        .map {
            it
                .drop(1)
                .dropLast(1)
        }

    val robots =
        mapBody.mapPos { x, y, char -> from(char)?.let { Robot(Pos(x, y), dir = it, id = RobotId(robotIds++)) } }

    val checkpoints = mapBody.mapPos { x, y, char ->
        char
            .digitToIntOrNull()
            ?.let { Checkpoint(order = it, pos = Pos(x, y), id = CheckpointId(checkpointId++)) }
    }

    val walls = mapBody.mapPrePos { x, y, char ->
        if (char == '|') Wall(
            Pos(x, y),
            Direction.Left,
        ) else null
    }

    return GameModel(
        robots = robots,
        actionDrawPile = actionCardDeck(),
        players = robots.map { Player(it.id, id = PlayerId(playerId++)) },
        course = Course(
            width = mapBody.mapPos{_,_,_ -> 0}.size,
            height = 1,
            checkpoints = checkpoints,
            conveyorBelts = emptyMap(),
            walls = walls
        )
    )
}

private fun assertValidMap(map: String) {
    val lines = map
        .trim()
        .split("\n")
    assert(
        lines
            .first()
            .matches("""^\+(\|\+)+$""".toRegex())
    ) { "Invalid format of first line" }
    lines
        .drop(1)
        .forEachIndexed { index, line ->
            val lineNo = index + 1
            assert(line.length == lines.first().length) { "line $lineNo should be the same length as the header line" }
            assert(line.first() == '+') { "line $lineNo should start with '+'" }
            assert(line.last() == '+') { "line $lineNo should end with '+'" }

            line.forEachIndexed { index, char ->
                val colNo = index + 1
                when (colNo.isEven) {
                    true -> assert(char in listOf(' ', '|')) { "invalid character ($char) at ${lineNo + 1}:$colNo" }
                    false -> assert(
                        char in listOf(
                            ' ',
                            '↓',
                            '→',
                            '←',
                            '↑',
                            '+',
                        ) + checkpoints()
                    ) { "invalid character ($char) at ${lineNo + 1}:$colNo" }
                }
            }
        }
}

private fun from(arrow: Char): Direction? = when (arrow) {
    '→' -> Direction.Right
    '←' -> Direction.Left
    '↑' -> Direction.Up
    '↓' -> Direction.Down
    else -> null
}

private fun checkpoints() = (1..9).map { it.digitToChar() }

private fun <T> List<String>.mapPos(cb: (x: Int, y: Int, char: Char) -> T?): List<T> =
    this
        .flatMapIndexed { y, row ->
            row
                .filterIndexed { index, _ -> index.isOdd }
                .mapIndexed { x, char ->
                    cb(x, y, char)
                }
        }
        .filterNotNull()


private fun <T> List<String>.mapPrePos(cb: (x: Int, y: Int, char: Char) -> T?): List<T> =
    this
        .flatMapIndexed { y, row ->
            row
                .filterIndexed { index, _ -> index.isEven }
                .mapIndexed { x, char ->
                    cb(x, y, char)
                }
        }
        .filterNotNull()


fun GameModel.dealCards(): GameModel = resolveDealActionCards().gameModel

fun GameModel.dealCards(cards: Map<PlayerId, List<ActionCard>>): GameModel = copy(
    players = players.map { it.copy(hand = cards.getValue(it.id)) }
)

fun GameModel.dealCards(vararg cards: Pair<PlayerId, List<ActionCard>>): GameModel = dealCards(cards.toMap())

fun GameModel.programAllRobots(cardsToDeal: Int = 5): GameModel = copy(
    robots = robots.map { r ->
        r.copy(
            registers = getPlayer(r.id).hand
                .take(cardsToDeal)
                .mapIndexed { index, card -> Register(card, index, false) }
                .toSet(),
        )
    },
    players = players.map { it.copy(hand = it.hand.drop(cardsToDeal)) },
)

fun GameModel.getRobotCards(robotId: RobotId): List<ActionCard> = getRobot(robotId).registers
    .sortedBy { it.index }
    .map { it.card }


class AnyOrderList<T> : MutableList<T> by mutableListOf() {
    override fun equals(other: Any?): Boolean {
        return if (other is List<*>)
            this.size == other.size && this.toSet() == other.toSet()
        else
            false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

fun <T> anyOrderList(vararg items: T): AnyOrderList<T> = anyOrderList(items.toList())

fun <T> anyOrderList(items: Collection<T>): AnyOrderList<T> {
    val list = AnyOrderList<T>()
    for (item in items) {
        list.add(item)
    }
    return list
}

fun GameModel.mapCourse(cb: (gameModel: GameModel, course: Course) -> Course): GameModel =
    copy(course = cb(this, this.course))

fun GameModel.checkpoints(): List<Checkpoint> = this.course.checkpoints.sortedBy { it.order }
