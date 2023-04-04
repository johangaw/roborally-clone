package gamemodel

import gamemodel.RoundResolution.*
import gamemodel.RoundStep.*

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): RoundResolutionResult {
    val phases = 1..programming.values.maxOf { it.size }
    return programming
        .map { (id, cards) -> cards.map { ResolveActionCard(id, it) } }
        .plus(listOf(phases.map { ResolveConveyorBelts }))
        .plus(listOf(phases.map { ResolveLasers }))
        .plus(listOf(phases.map { ResolveCheckpoints }))
        .plus(listOf(phases.map { CheckForWinner }))
        .zipAll()
        .map { it.sort() }
        .flatten()
        .plus(WipeRegisters)
        .plus(DealCards)
        .fold(RoundResolutionResult(assignRegisters(programming), emptyList())) { current, step ->
            if (current.resolutions.lastOrNull() is WinnerResolution) return current

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
                                resolutions = current.resolutions + LaserResolution(
                                    it.laserPaths,
                                    it.damage,
                                    it.lockedRegisters
                                )
                            )
                        }
                }

                CheckForWinner -> current.gameModel
                    .checkForWinner()
                    .let {
                        when (it) {
                            CheckForWinnerResult.NoWinnerFound -> current
                            is CheckForWinnerResult.WinnerFound -> RoundResolutionResult(
                                gameModel = current.gameModel,
                                resolutions = current.resolutions + WinnerResolution(it.playerId)
                            )
                        }
                    }

                WipeRegisters -> current.gameModel
                    .resolveWipeRegisters()
                    .let {
                        RoundResolutionResult(
                            gameModel = it.gameModel,
                            resolutions = current.resolutions + WipeRegistersResolution(it.lockedRegisters),
                        )
                    }

                DealCards -> current.gameModel
                    .resolveDealActionCards()
                    .let {
                        RoundResolutionResult(
                            gameModel = it.gameModel,
                            resolutions = current.resolutions + DealCardsResolution(it.hands),
                        )
                    }

                ResolveConveyorBelts -> current.gameModel
                    .resolveConveyorBelts()
                    .let {
                        RoundResolutionResult(
                            gameModel = it.gameModel,
                            resolutions = current.resolutions + ConveyorBeltsResolution(
                                it.movedRobots,
                                it.rotatedRobots
                            ),
                        )
                    }
            }
        }
}

private fun GameModel.assignRegisters(prog: Map<PlayerId, List<ActionCard>>): GameModel = copy(
    robots = robots.map {
        it.copy(
            registers = prog[getPlayer(it.id).id]
                ?.mapIndexed { index, card -> Register(card, index, false) }
                ?.toSet() ?: emptySet()
        )
    },
    players = players.map {
        it.copy(
            hand = it.hand - prog
                .getOrDefault(it.id, emptyList())
                .toSet()
        )
    }
)


private fun List<RoundStep>.sort(): List<RoundStep> =
    this.sortedBy {
        when (it) {
            is ResolveActionCard -> it.card.initiative
            ResolveCheckpoints -> Int.MAX_VALUE
            ResolveLasers -> Int.MAX_VALUE
            CheckForWinner -> Int.MAX_VALUE
            ResolveConveyorBelts -> Int.MAX_VALUE
            WipeRegisters -> throw AssertionError("WipeRegisters should not be sorted with other RoundSteps")
            DealCards -> throw AssertionError("DealCards should not be sorted with other RoundSteps")
        }
    }

private fun <T> List<List<T>>.zipAll(): List<List<T>> {
    return (0..this.minOf { it.lastIndex })
        .map { index -> this.map { it[index] } }
}

private sealed class RoundStep {
    data class ResolveActionCard(val playerId: PlayerId, val card: ActionCard) : RoundStep()

    object ResolveCheckpoints : RoundStep()

    object ResolveConveyorBelts : RoundStep()

    object ResolveLasers : RoundStep()

    object CheckForWinner : RoundStep()

    object WipeRegisters : RoundStep()

    object DealCards : RoundStep()
}

data class RoundResolutionResult(val gameModel: GameModel, val resolutions: List<RoundResolution>)

sealed class RoundResolution {
    data class ActionCardResolution(val steps: List<ActionCardResolutionStep>) : RoundResolution() {
        constructor(vararg steps: ActionCardResolutionStep) : this(steps.toList())
    }

    data class ConveyorBeltsResolution(val movedRobots: Map<RobotId, Pos>, val rotatedRobots: Map<RobotId, Direction>) :
        RoundResolution()

    data class CheckpointResolution(val capturedCheckpoints: Map<PlayerId, CheckpointId>) : RoundResolution()

    data class LaserResolution(
        val laserPaths: Set<LaserPath>,
        val damage: Map<RobotId, Int>,
        val lockedRegisters: Map<RobotId, List<LockedRegister>>,
    ) : RoundResolution()

    data class WinnerResolution(val winner: PlayerId) : RoundResolution()

    data class WipeRegistersResolution(val lockedRegisters: Map<RobotId, List<LockedRegister>>) : RoundResolution()

    data class DealCardsResolution(val hands: Map<PlayerId, List<ActionCard>>) : RoundResolution()
}
