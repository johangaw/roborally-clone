package gamemodel

fun GameModel.resolveWipeRegisters(): ResolveWipeRegistersResult {
    return ResolveWipeRegistersResult(
        gameModel = copy(
            actionDiscardPile = actionDiscardPile + players.flatMap {
                it.hand + getRobot(it.robotId).registers
                    .unlocked()
                    .cards()
                    .toSet()
            },
            robots = robots.map {
                it.copy(
                    registers = it.registers
                        .locked()
                        .toSet(),
                )
            },
            players = players.map {
                it.copy(hand = emptyList())
            }
        ),
        lockedRegisters = robots
            .associate {
                it.id to it.registers
                    .locked()
                    .map { register -> LockedRegister(register.index, register.card) }
            }
            .filterValues { it.isNotEmpty() },
    )
}

data class ResolveWipeRegistersResult(
    val gameModel: GameModel,
    val lockedRegisters: Map<RobotId, List<LockedRegister>>,
)
