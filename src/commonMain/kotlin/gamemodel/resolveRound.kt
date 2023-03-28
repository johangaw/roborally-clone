package gamemodel

import gamemodel.RoundResolution.*
import gamemodel.RoundStep.ResolveActionCard
import gamemodel.RoundStep.ResolveCheckpoints
import gamemodel.RoundStep.ResolveLasers

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): RoundResolutionResult {
    val phases = 1..programming.values.maxOf { it.size }
    return programming
        .map { (id, cards) -> cards.map { ResolveActionCard(id, it) } }
        .plus(listOf(phases.map { ResolveCheckpoints }))
        .plus(listOf(phases.map { ResolveLasers }))
        .zipAll()
        .map { it.sort() }
        .flatten()
        .fold(RoundResolutionResult(assignRegisters(programming), emptyList())) { current, step ->
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

                ResolveLasers -> {
                    current.gameModel
                        .resolveLasers()
                        .let {
                            RoundResolutionResult(
                                gameModel = it.gameModel,
                                resolutions = current.resolutions + LaserResolution(it.laserPaths, it.damage, it.lockedRegisters)
                            )
                        }
                }
            }
        }
}

private fun GameModel.assignRegisters(prog: Map<PlayerId, List<ActionCard>>): GameModel = copy(
    robots = robots.map { it.copy(
        registers = prog[getPlayer(it.id).id]?.mapIndexed {index, card -> Register(card, index, false) }?.toSet() ?: emptySet()
    ) }
)


private fun List<RoundStep>.sort(): List<RoundStep> =
    this.sortedBy {
        when(it) {
            is ResolveActionCard -> it.card.initiative
            ResolveCheckpoints -> Int.MAX_VALUE
            ResolveLasers -> Int.MAX_VALUE
        }
    }

private fun <T> List<List<T>>.zipAll(): List<List<T>> {
    return (0..this.minOf { it.lastIndex })
        .map { index -> this.map { it[index] } }
}

private sealed class RoundStep {
    data class ResolveActionCard(val playerId: PlayerId, val card: ActionCard) : RoundStep()

    object ResolveCheckpoints : RoundStep()

    object ResolveLasers : RoundStep()
}

data class RoundResolutionResult(val gameModel: GameModel, val resolutions: List<RoundResolution>)

sealed class RoundResolution {
    data class ActionCardResolution(val steps: List<ActionCardResolutionStep>) : RoundResolution() {
        constructor(vararg steps: ActionCardResolutionStep) : this(steps.toList())
    }

    data class CheckpointResolution(val capturedCheckpoints: Map<PlayerId, CheckpointId>) : RoundResolution()

    data class LaserResolution(
        val laserPaths: Set<LaserPath>,
        val damage: Map<RobotId, Int>,
        val lockedRegisters: Map<RobotId, List<LockedRegister>>
    ) : RoundResolution()
}
