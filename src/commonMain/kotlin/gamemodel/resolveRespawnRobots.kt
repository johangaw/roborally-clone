package gamemodel

fun <T, S> Collection<T>.cartesianProduct(other: Iterable<S>): List<Pair<T, S>> {
    return this.flatMap { first -> other.map { second -> first to second } }
}

fun surroundingPositions(pos: Pos): List<Pos> =
    listOf(-1, 0, 1)
        .let { diffs ->
            diffs
                .map { pos.x + it }
                .cartesianProduct(diffs.map { pos.y + it })
        }
        .map { Pos(it.first, it.second) }
        .minus(pos)

fun GameModel.resolveRespawnRobots(): ResolveRespawnRobotsResult {

    fun preferredSpawnPositions(robot: Robot): List<Pos> {
        val player = getPlayer(robot.id)
        val lastCapturedCheckpoint = player.capturedCheckpoints.maxOfOrNull { course.getCheckpoint(it) }
        return if (lastCapturedCheckpoint != null)
            listOf(lastCapturedCheckpoint.pos) + surroundingPositions(lastCapturedCheckpoint.pos)
        else
            course.starts
                .sorted()
                .map { it.pos }
    }

    val spawnPositions = destroyedRobots
        .groupBy(::preferredSpawnPositions)
        .flatMap { (suggestions, robots) ->
            robots
                .zip(suggestions.filter { !course.isMissingFloor(it) })
                .map { (robot, pos) -> robot.copy(pos = pos) }
        }

    return ResolveRespawnRobotsResult(
        this.copy(
            destroyedRobots = emptyList(),
            robots = robots + spawnPositions
        ),
        spawnPositions
    )
}

data class ResolveRespawnRobotsResult(
    val gameModel: GameModel,
    val spawnedRobots: List<Robot>,
)
