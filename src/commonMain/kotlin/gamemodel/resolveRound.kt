package gamemodel

import gamemodel.RoundResolution.*
import gamemodel.RoundStep.*

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): RoundResolutionResult {
    assertValidProgramming(programming)

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
        .plus(RespawnRobots)
        .plus(ResolveRegisterLocking)
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
                                resolutions = current.resolutions + it.toResolution()
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
                                    it.remainingHealthOfDamagedRobots,
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
                                it.rotatedRobots,
                                it.remainingHealthOfFallenRobots,
                            ),
                        )
                    }

                RespawnRobots -> current.gameModel
                    .resolveRespawnRobots()
                    .let {
                        RoundResolutionResult(
                            gameModel = it.gameModel,
                            resolutions = current.resolutions + SpawnedRobotsResolution(it.spawnedRobots),
                        )
                    }

                ResolveRegisterLocking -> current.gameModel
                    .resolveRegisterLocking()
                    .let {
                        RoundResolutionResult(
                            gameModel = it.gameModel,
                            resolutions = current.resolutions + RegisterLockingResolution(it.lockedRegisters),
                        )
                    }
            }
        }
}

fun GameModel.assertValidProgramming(programming: Map<PlayerId, List<ActionCard>>) {
    programming.forEach { (id, cards) ->
        val player = getPlayer(id)
        val robot = getRobot(id)
        val availableCards = player.hand + robot.registers.map { it.card }
        assert(availableCards.containsAll(cards)) { "Player $id have programmed an illegal card" }
        assert(
            robot.registers
                .filter { it.locked }
                .all { cards[it.index] == it.card }) {
            "Player $id have programmed a card even though another cards was locked"
        }
    }
}

private fun ActionCardResolutionResult.toResolution() = when (this) {
    is ActionCardResolutionResult.MovementResult -> ActionCardMovementResolution(this.steps)
    is ActionCardResolutionResult.TurningResult -> ActionCardRotationResolution(this.robotId, this.newDirection)
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
            RespawnRobots -> throw AssertionError("RespawnRobots should not be sorted with other RoundSteps")
            ResolveRegisterLocking -> throw AssertionError("ResolveRegisterLocking should not be sorted with other RoundSteps")
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

    object RespawnRobots : RoundStep()

    object ResolveRegisterLocking : RoundStep()

    object WipeRegisters : RoundStep()

    object DealCards : RoundStep()
}

data class RoundResolutionResult(val gameModel: GameModel, val resolutions: List<RoundResolution>)

sealed class RoundResolution {
    data class ActionCardMovementResolution(val steps: List<MovementStep>) : RoundResolution() {
        constructor(vararg steps: MovementStep) : this(steps.toList())
    }

    data class ActionCardRotationResolution(val robotId: RobotId, val newDirection: Direction) : RoundResolution()

    data class ConveyorBeltsResolution(
        val movedRobots: Map<RobotId, Pos>,
        val rotatedRobots: Map<RobotId, Direction>,
        val remainingHealthOfFallenRobots: Map<RobotId, Int>,
    ) :
        RoundResolution()

    data class CheckpointResolution(val capturedCheckpoints: Map<PlayerId, CheckpointId>) : RoundResolution()

    data class LaserResolution(
        val laserPaths: Set<LaserPath>,
        val remainingHealthOfDamagedRobots: Map<RobotId, Int>,
    ) : RoundResolution()

    data class WinnerResolution(val winner: PlayerId) : RoundResolution()

    data class SpawnedRobotsResolution(val spawnedRobots: List<Robot>) : RoundResolution()

    data class RegisterLockingResolution(val lockedRegisters: Map<RobotId, List<LockedRegister>>) : RoundResolution()

    data class WipeRegistersResolution(val lockedRegisters: Map<RobotId, List<LockedRegister>>) : RoundResolution()

    data class DealCardsResolution(val hands: Map<PlayerId, List<ActionCard>>) : RoundResolution()
}
