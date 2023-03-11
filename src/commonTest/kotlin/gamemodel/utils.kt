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
    assertValidMap(map)

    val mapBody = map.split("\n").drop(1).map { it.drop(1).dropLast(1) }

    return GameModel(
        robots = mapBody
            .flatMapIndexed { y, row ->
                row.filterIndexed { index, _ -> index.isOdd }
                    .mapIndexed { x, char ->
                        val dir = from(char)
                        dir?.let { Robot(Pos(x, y), dir, id = RobotId(robotIds++)) }
                    }
            }.filterNotNull(),
        walls = mapBody
            .flatMapIndexed { y, row ->
                row.filterIndexed { index, _ -> index.isEven }
                    .mapIndexed { x, char ->
                        if (char == '|') Wall(Pos(x, y), Direction.Left, id = WallId(wallIds++)) else null
                    }
            }.filterNotNull(),
        actionDrawPile = actionCardDeck(),
        players = emptyList()
    )
}

private fun assertValidMap(map: String) {
    val lines = map.trim().split("\n")
    assert(lines.first().matches("""^\+(\|\+)+$""".toRegex())) { "Invalid format of first line" }
    lines.drop(1).forEachIndexed { index, line ->
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
                        '+'
                    )
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
