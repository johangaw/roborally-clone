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
        ).mapCourse { model, course ->
            course.copy(
                checkpoints = listOf(
                    Checkpoint(1, model.robots.first().pos)
                )
            )
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
            course.copy(
                checkpoints = course.checkpoints + Checkpoint(2, gameModel.robots.first().pos),
            )
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
