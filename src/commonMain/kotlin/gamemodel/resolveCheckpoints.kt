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

data class CheckpointResolutionResult(
    val gameModel: GameModel,
    val capturedCheckpoints: Map<PlayerId, CheckpointId>
)
