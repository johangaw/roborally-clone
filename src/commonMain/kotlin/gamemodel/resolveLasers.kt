package gamemodel

import kotlin.math.max

fun GameModel.resolveLasers(): LaserResolutionResult {
    val laserPaths = robots
        .mapNotNull { robot ->
            laserPath(robot.pos, robot.dir, targetRobotAtStartPosition = false)
                .takeIf { it.isNotEmpty() }
                ?.let { LaserPath(it, robot.dir) }
        }
        .plus(
            course.laserCannons.mapNotNull { cannon ->
                laserPath(cannon.pos, cannon.dir, targetRobotAtStartPosition = true)
                    .takeIf { it.isNotEmpty() }
                    ?.let { LaserPath(it, cannon.dir) }
            }
        )
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
        hitRobots.mapValues { (_, data) -> data.health },
        laserPaths,
    )
}

private data class DamagedRobot(
    val id: RobotId,
    val health: Int,
    val damage: Int,
)

private fun GameModel.laserPath(startPosition: Pos, dir: Direction, targetRobotAtStartPosition: Boolean): List<Pos> {
    return (1..max(course.width, course.height))
        .runningFold(startPosition) { acc, _ -> acc + dir }
        .takeWhileIncludingStop { wallAt(it, dir) == null }
        .let { if (targetRobotAtStartPosition) it else it.drop(1)  }
        .takeWhileIncludingStop { robotAt(it) == null }
        .takeWhileIncludingStop { course.isOnCourse(it) }
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

data class LaserPath(val path: List<Pos>, val dir: Direction)
