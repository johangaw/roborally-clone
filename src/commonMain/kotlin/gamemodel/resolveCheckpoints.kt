package gamemodel

fun GameModel.resolveCheckpoints(): CheckpointResolutionResult {
    val capturedCheckpoints = robots
        .filter { nextCheckpoint(it.id)?.pos == it.pos }
        .associate { getPlayer(it.id).id to nextCheckpoint(it.id)!!.id }

    return CheckpointResolutionResult(
        gameModel = copy(
            players = players.map {
                if (it.id in capturedCheckpoints) it.copy(
                    capturedCheckpoints = it.capturedCheckpoints + capturedCheckpoints.getValue(it.id)
                ) else it
            }
        ),
        capturedCheckpoints = capturedCheckpoints
    )
}

private fun GameModel.nextCheckpoint(robotId: RobotId): Checkpoint? = getPlayer(robotId).capturedCheckpoints
    .map { course.getCheckpoint(it).order }
    .fold(-1) { maxOrder, order -> Integer.max(maxOrder, order) }
    .let { maxOrderCompleted ->
        course.checkpoints
            .sortedBy { it.order }
            .firstOrNull { maxOrderCompleted < it.order }
    }

data class CheckpointResolutionResult(
    val gameModel: GameModel,
    val capturedCheckpoints: Map<PlayerId, CheckpointId>
)
