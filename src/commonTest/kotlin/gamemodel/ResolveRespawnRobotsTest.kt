package gamemodel

import kotlin.test.*

class ResolveRespawnRobotsTest {

    @Test
    fun `when no robots are destroyed, it does nothing`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ←         +
        """.trimIndent()
        )

        val result = model.resolveRespawnRobots()

        assertEquals(model, result.gameModel)
        assertEquals(emptyList(), result.spawnedRobots)
    }

    @Test
    fun `when there are one destroyed robot, it is spawned on its original start location`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ←         +
        """.trimIndent()
        )
            .let {
                val (r1) = it.robots
                it
                    .copy(
                        robots = emptyList(),
                        destroyedRobots = listOf(r1.copy(pos = Pos(-1, 0))),
                    )
            }
            .mapCourse { _, course -> course.copy(starts = listOf(Start(Pos(3, 0), 0))) }
        val newR1 = model.destroyedRobots
            .first()
            .copy(pos = Pos(3, 0))
        val expectedModel = model.copy(robots = listOf(newR1), destroyedRobots = emptyList())

        val result = model.resolveRespawnRobots()

        assertEquals(listOf(newR1), result.spawnedRobots)
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `when there is one destroyed robot with a captured checkpoint, it is spawned on that checkpoint`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ←   1     +
        """.trimIndent()
        )
            .let {
                val (r1) = it.robots
                val (p1) = it.players
                val (c1) = it.course.checkpoints
                it
                    .copy(
                        robots = emptyList(),
                        players = listOf(p1.copy(capturedCheckpoints = listOf(c1.id))),
                        destroyedRobots = listOf(r1.copy(pos = Pos(-1, 0))),
                    )
            }
        val newR1 = model.destroyedRobots
            .first()
            .copy(pos = Pos(2, 0))
        val expectedModel = model.copy(
            robots = listOf(newR1),
            destroyedRobots = emptyList(),
        )

        val result = model.resolveRespawnRobots()

        assertEquals(listOf(newR1), result.spawnedRobots)
        assertEquals(expectedModel, result.gameModel)
    }

    @Test
    fun `when there are two destroyed robots with the same captured checkpoint, one is spawned on the checkpoint and the other is spawned next to it on a free position`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + 1 ←     → +
        """.trimIndent()
        )
            .let {
                val (r1, r2) = it.robots
                val (p1, p2) = it.players
                val (c1) = it.course.checkpoints
                it
                    .copy(
                        robots = emptyList(),
                        players = listOf(
                            p1.copy(capturedCheckpoints = listOf(c1.id)),
                            p2.copy(capturedCheckpoints = listOf(c1.id))
                        ),
                        destroyedRobots = listOf(r1.copy(pos = Pos(-1, 0)), r2.copy(pos = Pos(5, 0))),
                    )
            }
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            + ← →       +
        """.trimIndent()
        )
            .mapCourse { _, _ -> model.course }
            .let {
                val (p1, p2) = it.players
                val (c1) = it.course.checkpoints
                it.copy(
                    players = listOf(
                        p1.copy(capturedCheckpoints = listOf(c1.id)),
                        p2.copy(capturedCheckpoints = listOf(c1.id))
                    ),
                    destroyedRobots = emptyList(),
                )
            }

        val result = model.resolveRespawnRobots()

        assertEquals(expectedModel.robots, result.spawnedRobots)
        assertEquals(expectedModel, result.gameModel)
    }
}
