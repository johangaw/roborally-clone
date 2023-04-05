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
                ?.let { pos -> robotAt(pos) }
        }
        .groupBy { it }
        .mapValues { (robot, hits) -> DamagedRobot(robot.id, robot.health - hits.size, hits.size) }
        .mapKeys { (robot, _) -> robot.id }
    return LaserResolutionResult(
        copy(robots = robots.map {
            it.copy(health = hitRobots[it.id]?.health ?: it.health)
        }),
        hitRobots.mapValues { (robotId, data) -> data.health },
        laserPaths,
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

private data class DamagedRobot(
    val id : RobotId,
    val health: Int,
    val damage: Int,
)

private fun GameModel.laserPath(pos: Pos, dir: Direction): List<Pos> {
    return (1..100)
        .runningFold(pos + dir) { acc, _ -> acc + dir }
        .takeWhileIncludingStop { wallAt(it, dir) == null }
        .takeWhileIncludingStop { robotAt(it) == null }
}

private fun <T> List<T>.takeWhileIncludingStop(predicate: (T) -> Boolean): List<T> =
    slice(0..(this
        .indexOfFirst { !predicate(it) }
        .takeIf { it >= 0 } ?: this.lastIndex))

data class LaserResolutionResult(
    val gameModel: GameModel,
    val remainingHealthOfDamagedRobots: Map<RobotId, Int>,
    val laserPaths: Set<LaserPath>,
)

data class LockedRegister(val index: Int, val card: ActionCard)

enum class LaserDirection {
    Right,
    Left,
    Down,
    Up
}

data class LaserPath(val path: List<Pos>, val dir: LaserDirection)
