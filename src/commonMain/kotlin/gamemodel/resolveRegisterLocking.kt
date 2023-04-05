package gamemodel

fun GameModel.resolveRegisterLocking(): ResolveRegisterLockingResult {
    val newRobots =
        robots.map { robot ->
            robot.copy(
                registers = robot.registers
                    .mapToSet { it.copy(locked = robot.health <= it.index + 1) }
            )
        }
    return ResolveRegisterLockingResult(
        gameModel = copy(robots = newRobots),
        lockedRegisters = newRobots
            .associate { robot ->
                robot.id to robot.registers
                    .sorted()
                    .locked()
                    .map { LockedRegister(it.index, it.card) }
            }
            .filterValues { it.isNotEmpty() },
    )
}

data class ResolveRegisterLockingResult(
    val gameModel: GameModel,
    val lockedRegisters: Map<RobotId, List<LockedRegister>>,
)
