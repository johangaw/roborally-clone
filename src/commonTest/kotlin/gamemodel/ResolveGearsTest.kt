package gamemodel

import kotlin.test.*

class ResolveGearsTest {

    @Test
    fun `when no robots stands on any gears, nothing happens`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )

        val result = model.resolveGears()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.rotatedRobots)
    }

    @Test
    fun `when a robots stands on a clockwise gears, it turns clockwise 90 degrees`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     →     +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                gears = mapOf(Pos(2, 0) to Gear(Rotation.Clockwise))
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(dir = Direction.Down))
        )

        val result = model.resolveGears()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r1.id to Direction.Down), result.rotatedRobots)
    }

    @Test
    fun `when a robots stands on a counter-clockwise gears, it turns counter-clockwise 90 degrees`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     →     +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                gears = mapOf(Pos(2, 0) to Gear(Rotation.CounterClockwise))
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(dir = Direction.Up))
        )

        val result = model.resolveGears()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r1.id to Direction.Up), result.rotatedRobots)
    }
}
