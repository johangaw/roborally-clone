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
        assertEquals(setOf(
            (-1 downTo -101).map { Pos(0, it) },
            (1 .. 101).map { Pos(2 + it, 0) },
        ), result.laserPaths)
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
                (1..4).map { Pos(it, 0) },
                (-1 downTo -101).map { Pos(4, it) },
            ),
            result.laserPaths
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
                (1..3).map { Pos(it, 0) },
                (1..101).map { Pos(3, it) },
                (-1 downTo -101).map { Pos(4, it) }

            ),
            result.laserPaths
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
                listOf(Pos( 1, 0)),
                (1..101).map { Pos(2, it) }
            ),
            result.laserPaths
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
}
