package gamemodel

import kotlin.test.*

class ResolveCheckpointsTest {

    @Test
    fun `when no robot is on a checkpoint, no checkpoint is captured`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → 1       +
        """.trimIndent()
        )

        val result = model.resolveCheckpoints()

        assertEquals(
            model,
            result.gameModel,
        )
        assertEquals(
            emptyMap(),
            result.capturedCheckpoints,
        )
    }

    @Test
    fun `when a robot ends on its next checkpoint, that checkpoint is captured`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )
            .mapCourse{model, course ->
                val pos = model.robots.first().pos
                course.copy(checkpoints = mapOf(
                    pos to Checkpoint(1, pos)
                ))
            }
        val (p1) = model.players
        val (c1) = model.checkpoints()
        val expectedModel = model.copy(
            players = listOf(p1.copy(capturedCheckpoints = listOf(c1.id))),
        )

        val result = model.resolveCheckpoints()

        assertEquals(
            expectedModel,
            result.gameModel,
        )
        assertEquals(
            mapOf(p1.id to c1.id),
            result.capturedCheckpoints,
        )
    }

    @Test
    fun `when a robot ends on its next next checkpoint, no checkpoint is captured`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   1     +
        """.trimIndent()
        ).mapCourse { gameModel, course ->
            val pos = gameModel.robots.first().pos
            course.copy(checkpoints = course.checkpoints + (pos to Checkpoint(2, pos)))
        }

        val result = model.resolveCheckpoints()

        assertEquals(
            model,
            result.gameModel,
        )
        assertEquals(
            emptyMap(),
            result.capturedCheckpoints,
        )
    }
}
