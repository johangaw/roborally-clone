package gamemodel

fun GameModel.resolveLasers(): LaserResolutionResult {
    val laserPaths = robots
        .map { robot -> laserPath(robot.pos, robot.dir).let { LaserPath(it, laserDirection(robot.pos, it.first())) } }
        .toSet()
    val hitRobots = laserPaths
        .mapNotNull {
            it
                .path
                .lastOrNull()
                ?.let { robotAt(it) }
        }
        .groupBy { it.id }
        .mapValues { (_, hits) -> hits.size }
    val lockedProgramming = hitRobots
        .mapKeys { (id, _) -> getRobot(id) }
        .mapValues { (r, hits) ->
            r.registers
                .filter { !it.locked }
                .filter { it.index >= r.health - hits - 1 }
        }
        .mapKeys { it.key.id }
        .mapValues { it.value.map { card -> card.card } }
        .filterValues { it.isNotEmpty() }
    return LaserResolutionResult(
        copy(robots = robots.map {
            it.copy(
                health = it.health - hitRobots.getOrDefault(it.id, 0),
                registers = it.registers.map { prog ->
                    if(prog.card in lockedProgramming.getOrDefault(it.id, emptyList()))
                        prog.copy(locked = true)
                    else
                        prog
                }.toSet()
            )
        }),
        hitRobots,
        laserPaths,
        lockedProgramming
    )
}

fun laserDirection(robotPos: Pos, beginningOfLaserPath: Pos): LaserDirection {
    val dx = beginningOfLaserPath.x - robotPos.x
    val dy = beginningOfLaserPath.y - robotPos.y
    return if (dx == 0)
        if (dy > 0) LaserDirection.Down
        else LaserDirection.Up
    else
        if (dx > 0) LaserDirection.Right
        else LaserDirection.Left
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
    val laserPaths: Set<LaserPath>,
    val lockedCards: Map<RobotId, List<ActionCard>>,
)

enum class LaserDirection {
    Right,
    Left,
    Down,
    Up
}

data class LaserPath(val path: List<Pos>, val dir: LaserDirection)
