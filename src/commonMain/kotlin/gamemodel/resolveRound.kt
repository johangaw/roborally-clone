package gamemodel

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): RoundResolutionResult {
//    if (programming.values.all { it.isEmpty() }) return RoundResolutionResult(this, emptyList())

    programming.forEach { (playerId, cards) ->
        assert(cards.isNotEmpty()) { "Player ${playerId.value} has selected fewer cards than the others" }
    }

    return programming.map { (id, cards) -> cards.map { id to it } }
        .zipAll()
        .map { it.sortedBy { (_, card) -> card.initiative } }
        .flatten()
        .fold(RoundResolutionResult(this, emptyList())) { current, (playerId, card) ->
            val result = current.gameModel.controlRobot(current.gameModel.getPlayer(playerId).robotId, card)
            RoundResolutionResult(
                gameModel = result.gameModel,
                resolutions = current.resolutions + ActionCardResolution(result.steps)
            )
        }
}


private fun <T>List<List<T>>.zipAll(): List<List<T>> {
    return (0..this.minOf { it.lastIndex })
        .map { index -> this.map { it[index] } }
}

data class RoundResolutionResult(val gameModel: GameModel, val resolutions: List<ActionCardResolution>)

//sealed class Resolution() {}  // TODO use for resolutions not corresponding to action action cards, converer-belts, lasers, etc
data class ActionCardResolution(val steps: List<ActionCardResolutionStep>) {
    constructor(vararg steps: ActionCardResolutionStep): this(steps.toList())
}
