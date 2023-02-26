package gamemodel

fun gameModel(map: String): GameModel {
    var robotIds = 0
    return GameModel(
        robots = map
            .split("\n")
            .flatMapIndexed { y, row ->
                row.mapIndexed { x, char ->
                    val dir = from(char)
                    dir?.let { Robot(Pos(x, y), dir, id = ResourceId(robotIds++)) }
                }
            }.filterNotNull()
    )
}

private fun from(arrow: Char): Direction? = when (arrow) {
    '→' -> Direction.Right
    '←' -> Direction.Left
    '↑' -> Direction.Up
    '↓' -> Direction.Down
    else -> null
}
