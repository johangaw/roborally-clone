package gamemodel

import kotlin.math.max

fun GameModel.resolveLasers(): LaserResolutionResult {
    val laserPaths = robots
        .mapNotNull { robot ->
            laserPath(robot.pos, robot.dir, targetRobotAtStartPosition = false)
                .takeIf { it.isNotEmpty() }
                ?.let { LaserPath(it, robot.dir, 1, LaserPathSource.Robot) }
        }
        .plus(
            course.laserCannons.mapNotNull { cannon ->
                laserPath(cannon.pos, cannon.dir, targetRobotAtStartPosition = true)
                    .takeIf { it.isNotEmpty() }
                    ?.let { LaserPath(it, cannon.dir, cannon.power, LaserPathSource.Cannon) }
            }
        )

    val damagedRobots = laserPaths
        .groupByTarget(this)
        .damagePerTarget()
        .applyDamage()
        .associateBy { it.id }

    return LaserResolutionResult(
        copy(robots = robots.map {
            it.copy(health = damagedRobots[it.id]?.remainingHealth ?: it.health)
        }),
        damagedRobots.mapOnlyValues { damagedRobot -> damagedRobot.remainingHealth },
        laserPaths.toSet(),
    )
}

private fun Collection<LaserPath>.groupByTarget(gameModel: GameModel) =
    map {
        it.path
            .lastOrNull()
            ?.let { pos -> gameModel.robotAt(pos) }
    }
        .zip(this)
        .filterNotNull()
        .groupBy { (target, _) -> target }
        .mapOnlyValues { paths -> paths.map { (_, path) -> path } }

private fun Map<Robot, List<LaserPath>>.damagePerTarget() =
    mapOnlyValues { laserPaths -> laserPaths.sumOf { it.power } }

private fun Map<Robot, Int>.applyDamage() =
    map { (robot, damage) -> DamagedRobot(robot.id, robot.health - damage, damage) }

private fun <A, B> Collection<Pair<A?, B?>>.filterNotNull(): Collection<Pair<A, B>> =
    mapNotNull { (first, second) ->
        if (first == null || second == null) null else first to second
    }

private fun <K, V, VV>Map<K, V>.mapOnlyValues(cb: (V) -> VV): Map<K, VV> =
    this.mapValues { (_, value) -> cb(value) }

private data class DamagedRobot(
    val id: RobotId,
    val remainingHealth: Int,
    val damage: Int,
)

private fun GameModel.laserPath(startPosition: Pos, dir: Direction, targetRobotAtStartPosition: Boolean): List<Pos> {
    return (1..max(course.width, course.height))
        .runningFold(startPosition) { acc, _ -> acc + dir }
        .takeWhileIncludingStop { wallAt(it, dir) == null }
        .let { if (targetRobotAtStartPosition) it else it.drop(1) }
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

data class LaserPath(val path: List<Pos>, val dir: Direction, val power: Int, val source: LaserPathSource)

enum class LaserPathSource {
    Robot, Cannon
}
