package gamemodel

import org.junit.Test
import kotlin.test.*

class ResolveLasersTest {

    @Test
    fun `when a robot is not hit by any lasers, it is not damaged`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ↑   →     +
        """.trimIndent()
        )

        val result = model.resolveLasers()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.damage)
        assertEquals(
            setOf(
                LaserPath(
                    (-1 downTo -101).map { Pos(0, it) },
                    LaserDirection.Up,
                ), LaserPath(
                    (1..101).map { Pos(2 + it, 0) }, LaserDirection.Right
                )
            ), result.laserPaths
        )
    }

    @Test
    fun `when a robot is hit by another robots forward laser, it takes one damage`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →       ↑ +
        """.trimIndent()
        )
        val (r1, r2) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1, r2.copy(health = 9))
        )

        val result = model.resolveLasers()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r2.id to 1), result.damage)
        assertEquals(
            setOf(
                LaserPath((1..4).map { Pos(it, 0) }, LaserDirection.Right),
                LaserPath((-1 downTo -101).map { Pos(4, it) }, LaserDirection.Up),
            ), result.laserPaths
        )
    }

    @Test
    fun `lasers does not penetrate robots`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →     ↓ ↑ +
        """.trimIndent()
        )
        val (r1, r2, r3) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1, r2.copy(health = 9), r3)
        )

        val result = model.resolveLasers()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r2.id to 1), result.damage)
        assertEquals(
            setOf(
                LaserPath((1..3).map { Pos(it, 0) }, LaserDirection.Right),
                LaserPath((1..101).map { Pos(3, it) }, LaserDirection.Down),
                LaserPath((-1 downTo -101).map { Pos(4, it) }, LaserDirection.Up),
            ), result.laserPaths
        )
    }

    @Test
    fun `lasers does not penetrate walls`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →  |↓     +
        """.trimIndent()
        )

        val result = model.resolveLasers()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.damage)
        assertEquals(
            setOf(
                LaserPath(listOf(Pos(1, 0)), LaserDirection.Right),
                LaserPath((1..101).map { Pos(2, it) }, LaserDirection.Down),
            ), result.laserPaths
        )
    }

    @Test
    fun `when a robot is hit by two robots fire upon each other, both are damaged`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →     ←   +
        """.trimIndent()
        )
        val (r1, r2) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(health = 9), r2.copy(health = 9))
        )

        val result = model.resolveLasers()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r1.id to 1, r2.id to 1), result.damage)
    }

    @Test
    fun `when a robot is hit by two lasers, it suffers two damage`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   ↓   ← +
        """.trimIndent()
        )
        val (r1, r2, r3) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1, r2.copy(health = 8), r3)
        )

        val result = model.resolveLasers()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r2.id to 2), result.damage)
    }

//    @Test
//    fun `when a robot takes their 4th damage no register is locked`() {
//        val model = gameModel(
//            """
//            +|+|+|+|+|+|+
//            + →   ↓     +
//        """.trimIndent(),
//            null
//        )
//            .dealCards()
//            .programAllRobots()
//        val (r1, r2) = model.robots
//        val expectedModel = model.copy(
//            robots = listOf(r1, r2.copy(health = 6))
//        )
//
//        val result = model.resolveLasers().gameModel.resolveLasers().gameModel.resolveLasers().gameModel.resolveLasers()
//
//        assertEquals(expectedModel, result.gameModel)
//        assertEquals(mapOf(), result.lockedRegisters)
//    }
//
//    @Test
//    fun `when a robot takes their 5th damage 1 register is locked`() {
//        val model = gameModel(
//            """
//            +|+|+|+|+|+|+
//            + →   ↓     +
//        """.trimIndent(),
//            null
//        )
//            .dealCards()
//            .programAllRobots()
//            .let {
//                val (r1, r2) = it.robots
//                it.copy(robots = listOf(r1, r2.copy(health = 6)))
//            }
//        val (r1, r2) = model.robots
//        val expectedModel = model.copy(
//            robots = listOf(r1, r2.copy(health = 5, registers = r2.registers.mapToSet {
//                when (it.index) {
//                    4 -> it.copy(locked = true)
//                    else -> it
//                }
//            }))
//        )
//
//        val result = model.resolveLasers()
//
//        assertEquals(expectedModel, result.gameModel)
//        assertEquals(
//            mapOf(
//                r2.id to listOf(
//                    r2.registers
//                        .last()
//                        .let { LockedRegister(it.index, it.card) })
//            ),
//            result.lockedRegisters,
//        )
//    }

//    @Test
//    fun `when a robots health is brought down from 6 to 4, two registers are locked at once`() {
//        val model = gameModel(
//            """
//            +|+|+|+|+|+|+
//            + →   ↓   ← +
//        """.trimIndent(),
//            9 * 3
//        )
//            .dealCards()
//            .programAllRobots()
//            .let {
//                val (r1, r2, r3) = it.robots
//                it.copy(robots = listOf(r1, r2.copy(health = 6), r3))
//            }
//        val (r1, r2, r3) = model.robots
//        val expectedModel = model.copy(robots = listOf(r1, r2.copy(health = 4, registers = r2.registers.mapToSet {
//            when (it.index) {
//                3, 4 -> it.copy(locked = true)
//                else -> it
//            }
//        }), r3))
//
//        val result = model.resolveLasers()
//
//        assertEquals(
//            expectedModel,
//            result.gameModel,
//        )
//        assertEquals(
//            mapOf(r2.id to r2.registers
//                .filter { it.index in listOf(3, 4) }
//                .map { LockedRegister(it.index, it.card) }),
//            result.lockedRegisters,
//        )
//    }
}
