package gamemodel

fun GameModel.checkForWinner(): CheckForWinnerResult =
    players
        .firstOrNull { player -> player.capturedCheckpoints.containsAll(this.course.checkpoints.map { it.id }) }
        ?.let {
            CheckForWinnerResult.WinnerFound(it.id)
        } ?: CheckForWinnerResult.NoWinnerFound


sealed class CheckForWinnerResult {
    data class WinnerFound(val playerId: PlayerId) : CheckForWinnerResult()
    object NoWinnerFound : CheckForWinnerResult()
}
