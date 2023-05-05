package gamemodel

fun GameModel.resolveGears(): ResolveGearsResult {
    val rotatedRobots = robots
        .mapNotNull { robot -> course.gears[robot.pos]?.let { robot to it } }
        .associate { (robot, gear) -> robot.id to robot.dir.rotate(gear.rotation) }

    return ResolveGearsResult(
        gameModel = copy(
            robots = robots.map { it.copy(dir = rotatedRobots.getOrDefault(it.id, it.dir)) }
        ),
        rotatedRobots
    )
}

data class ResolveGearsResult(val gameModel: GameModel, val rotatedRobots: Map<RobotId, Direction>)
