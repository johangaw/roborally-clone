package gamemodel

import gamemodel.RoundResolution.ActionCardResolution
import gamemodel.RoundResolution.CheckpointResolution
import gamemodel.RoundStep.ResolveActionCard
import gamemodel.RoundStep.ResolveCheckpoints

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): RoundResolutionResult {
    return programming
        .map { (id, cards) -> cards.map { ResolveActionCard(id, it) } }
        .zipAll()
        .map { it.sortedBy { (_, card) -> card.initiative } }
        .plus(listOf((1..programming.values.maxOf { it.size }).map { ResolveCheckpoints }))
        .flatten()
        .fold(RoundResolutionResult(this, emptyList())) { current, step ->
            when (step) {
                is ResolveActionCard -> {
                    current.gameModel
                        .resolveActionCard(current.gameModel.getPlayer(step.playerId).robotId, step.card)
                        .let {
                            RoundResolutionResult(
                                gameModel = it.gameModel,
                                resolutions = current.resolutions + ActionCardResolution(it.steps)
                            )
                        }
                }

                ResolveCheckpoints -> {
                    current.gameModel
                        .resolveCheckpoints()
                        .let {
                            RoundResolutionResult(
                                gameModel = it.gameModel,
                                resolutions = current.resolutions + CheckpointResolution(it.capturedCheckpoints)
                            )
                        }
                }
            }
        }
}


private fun <T> List<List<T>>.zipAll(): List<List<T>> {
    return (0..this.minOf { it.lastIndex })
        .map { index -> this.map { it[index] } }
}

private sealed class RoundStep {
    data class ResolveActionCard(val playerId: PlayerId, val card: ActionCard) : RoundStep()

    object ResolveCheckpoints : RoundStep()
}

data class RoundResolutionResult(val gameModel: GameModel, val resolutions: List<RoundResolution>)

sealed class RoundResolution {
    data class ActionCardResolution(val steps: List<ActionCardResolutionStep>) : RoundResolution() {
        constructor(vararg steps: ActionCardResolutionStep) : this(steps.toList())
    }

    data class CheckpointResolution(val capturedCheckpoints: Map<PlayerId, CheckpointId>) : RoundResolution()
}
