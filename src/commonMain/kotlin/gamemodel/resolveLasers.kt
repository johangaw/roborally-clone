package gamemodel

fun GameModel.resolveLasers(): LaserResolutionResult {
    val laserPaths = robots
        .map { laserPath(it.pos, it.dir) }
        .toSet()
    val hitRobots = laserPaths
        .mapNotNull {
            it
                .lastOrNull()
                ?.let { robotAt(it) }
        }
        .groupBy { it.id }
        .mapValues { (_, hits) -> hits.size }
    return LaserResolutionResult(
        copy(robots = robots.map { it.copy(health = it.health - hitRobots.getOrDefault(it.id, 0)) }),
        hitRobots,
        laserPaths
    )
}

private fun GameModel.laserPath(pos: Pos, dir: Direction): List<Pos> {
    return (1..100)
        .runningFold(pos + dir) { pos, _ -> pos + dir }
        .takeWhileIncludingStop { wallAt(it, dir) == null }
        .takeWhileIncludingStop { robotAt(it) == null }
}

private fun <T> List<T>.takeWhileIncludingStop(predicate: (T) -> Boolean): List<T> =
    slice(0..(this
        .indexOfFirst { !predicate(it) }
        .takeIf { it >= 0 } ?: this.lastIndex))


data class LaserResolutionResult(
    val gameModel: GameModel,
    val damage: Map<RobotId, Int>,
    val laserPaths: Set<List<Pos>>,
)
