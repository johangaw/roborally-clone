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
        assertEquals(emptyMap(), result.remainingHealthOfDamagedRobots)
        assertEquals(
            setOf(
                LaserPath((-1..-1).map { Pos(0, it) }, Direction.Up,1, LaserPathSource.Robot),
                LaserPath((3..5).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot)
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
        assertEquals(mapOf(r2.id to 9), result.remainingHealthOfDamagedRobots)
        assertEquals(
            setOf(
                LaserPath((1..4).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((-1..-1).map { Pos(4, it) }, Direction.Up, 1, LaserPathSource.Robot),
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
        assertEquals(mapOf(r2.id to 9), result.remainingHealthOfDamagedRobots)
        assertEquals(
            setOf(
                LaserPath((1..3).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((1..1).map { Pos(3, it) }, Direction.Down, 1, LaserPathSource.Robot),
                LaserPath((-1..-1).map { Pos(4, it) }, Direction.Up, 1, LaserPathSource.Robot),
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
        assertEquals(emptyMap(), result.remainingHealthOfDamagedRobots)
        assertEquals(
            setOf(
                LaserPath(listOf(Pos(1, 0)), Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((1..1).map { Pos(2, it) }, Direction.Down, 1, LaserPathSource.Robot),
            ), result.laserPaths
        )
    }

    @Test
    fun `laser can't be fired into a wall`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +   →|      +
        """.trimIndent()
        )

        val result = model.resolveLasers()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.remainingHealthOfDamagedRobots)
        assertEquals(emptySet(), result.laserPaths)
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
        assertEquals(mapOf(r1.id to 9, r2.id to 9), result.remainingHealthOfDamagedRobots)
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
        assertEquals(mapOf(r2.id to 8), result.remainingHealthOfDamagedRobots)
    }

    @Test
    fun `when a robot is hit by a powerful laser, it suffers damage equal the the lasers power`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +|    →     +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                laserCannons = listOf(LaserCannon(Pos(0, 0), Direction.Right, 3))
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(health = 7))
        )

        val result = model.resolveLasers()

        assertEquals(
            setOf(
                LaserPath((3..5).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((0..2).map { Pos(it, 0) }, Direction.Right, 3, LaserPathSource.Cannon),
            ),
            result.laserPaths
        )
        assertEquals(mapOf(r1.id to 7), result.remainingHealthOfDamagedRobots)
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `this also fires the cannons on the course`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +|    →     +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                laserCannons = listOf(LaserCannon(Pos(0, 0), Direction.Right, 1))
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(health = 9))
        )

        val result = model.resolveLasers()

        assertEquals(
            setOf(
                LaserPath((3..5).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((0..2).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Cannon),
            ),
            result.laserPaths
        )
        assertEquals(mapOf(r1.id to 9), result.remainingHealthOfDamagedRobots)
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `when a cannon fires, it may hit robots on its same position`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +|→         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                laserCannons = listOf(LaserCannon(Pos(0, 0), Direction.Right, 1))
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(health = 9))
        )

        val result = model.resolveLasers()

        assertEquals(
            setOf(
                LaserPath((1..5).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Robot),
                LaserPath((0..0).map { Pos(it, 0) }, Direction.Right, 1, LaserPathSource.Cannon),
            ),
            result.laserPaths
        )
        assertEquals(mapOf(r1.id to 9), result.remainingHealthOfDamagedRobots)
        assertEquals(expectedModel, result.gameModel)
    }
}
