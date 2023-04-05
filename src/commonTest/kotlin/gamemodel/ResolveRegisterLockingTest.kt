package gamemodel

import kotlin.test.*

class ResolveRegisterLockingTest {

    private fun oneRobotModel() = gameModel(
        """
            +|+|+|+|+|+|+
            + ←         +
        """.trimIndent()
    )
        .dealCards()
        .programAllRobots()

    @Test
    fun `when no robots have 5 health or less, it does nothing`() {
        val model = oneRobotModel()

        val result = model.resolveRegisterLocking()

        assertEquals(emptyMap(), result.lockedRegisters)
        assertEquals(model, result.gameModel)
    }

    @Test
    fun `when a robot has 5 health, it locks 1 register`() {
        val model = oneRobotModel()
            .let {
                val (r1) = it.robots
                it.copy(
                    robots = listOf(r1.copy(health = 5))
                )
            }
        val (r1) = model.robots
        val expectedModel =
            model.copy(robots = listOf(r1.copy(registers = r1.registers.mapToSet { if (it.index == 4) it.copy(locked = true) else it })))

        val result = model.resolveRegisterLocking()

        assertEquals(
            mapOf(r1.id to listOf(LockedRegister(4, r1.registers.max().card))),
            result.lockedRegisters,
        )
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `when a robot has 1 health, it locks all register`() {
        val model = oneRobotModel()
            .let {
                val (r1) = it.robots
                it.copy(
                    robots = listOf(r1.copy(health = 1))
                )
            }
        val (r1) = model.robots
        val expectedModel =
            model.copy(robots = listOf(r1.copy(registers = r1.registers.mapToSet { it.copy(locked = true) })))

        val result = model.resolveRegisterLocking()

        assertEquals(
            mapOf(
                r1.id to listOf(
                    LockedRegister(0, r1.registers.sorted()[0].card),
                    LockedRegister(1, r1.registers.sorted()[1].card),
                    LockedRegister(2, r1.registers.sorted()[2].card),
                    LockedRegister(3, r1.registers.sorted()[3].card),
                    LockedRegister(4, r1.registers.sorted()[4].card),
                )
            ),
            result.lockedRegisters,
        )
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `when a robot previously had a locked register but has now more than 5 health, that register is unlocked`() {
        val model = oneRobotModel()
            .let {
                val (r1) = it.robots
                it.copy(
                    robots = listOf(
                        r1.copy(
                            health = 10,
                            registers = r1.registers.mapToSet { reg -> reg.copy(locked = true) },
                        )
                    )
                )
            }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(registers = r1.registers.mapToSet { it.copy(locked = false) }))
        )

        val result = model.resolveRegisterLocking()

        assertEquals(emptyMap(), result.lockedRegisters)
        assertEquals(expectedModel, result.gameModel)
    }
}
