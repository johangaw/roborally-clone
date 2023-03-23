package gamemodel

import gamemodel.ActionCardResolutionStep.MovementStep
import kotlin.test.*

class MoveRobotTest {

    @Test
    fun `when there is nothing blocking the robot it is moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )
        val robot = model.robots.first()
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +       →   +
        """.trimIndent()
        )

        val result = model.resolveActionCard(robot.id, ActionCard.MoveForward(3, 0))

        assertEquals(
            expectedModel,
            result.gameModel
        )

        assertEquals(
            listOf(
                MovementStep(robot.id to Pos(1, 0)),
                MovementStep(robot.id to Pos(2, 0)),
                MovementStep(robot.id to Pos(3, 0)),
            ),
            result.steps
        )
    }

    @Test
    fun `when a wall is blocking the robots path, is does not move through that wall`() {
        val model = gameModel(
            """
        +|+|+|+|+|+
        +    |  ← +
        """.trimIndent()
        )
        val robot = model.robots.first()
        val expectedModel = gameModel(
            """
        +|+|+|+|+|+
        +    |←   +
        """.trimIndent()
        )

        val result = model.resolveActionCard(robot.id, ActionCard.MoveForward(3, 0))

        assertEquals(
            expectedModel,
            result.gameModel
        )
        assertEquals(
            listOf(
                MovementStep(robot.id to Pos(2, 0))
            ),
            result.steps
        )
    }

    @Test
    fun `when another robot blocks the way it is push along`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   ↓     +
        """.trimIndent()
        )
        val pusher = model.robots[0]
        val pushed = model.robots[1]
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +       → ↓ +
        """.trimIndent()
        )

        val result = model.resolveActionCard(pusher.id, ActionCard.MoveForward(3, 0))

        assertEquals(
            expectedModel,
            result.gameModel
        )
        assertEquals(
            listOf(
                MovementStep(pusher.id to Pos(1, 0)),
                MovementStep(
                    pusher.id to Pos(2, 0),
                    pushed.id to Pos(3, 0),
                ),
                MovementStep(
                    pusher.id to Pos(3, 0),
                    pushed.id to Pos(4, 0),
                )
            ), result.steps
        )
    }

    @Test
    fun `when another robot blocks the way it is pushed along but not through walls`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   ↓   ↑|+
        """.trimIndent()
        )
        val pusher = model.robots[0]
        val pushed = model.robots[1]
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +     → ↓ ↑|+
        """.trimIndent()
        )

        val result = model.resolveActionCard(pusher.id, ActionCard.MoveForward(3, 0))

        assertEquals(
            expectedModel,
            result.gameModel
        )
        assertEquals(
            listOf(
                MovementStep(pusher.id to Pos(1, 0)),
                MovementStep(
                    pusher.id to Pos(2, 0),
                    pushed.id to Pos(3, 0),
                )
            ), result.steps
        )
    }

    @Test
    fun `when the robot tries to moves backwards and nothing is blocking the way, It moved backwards`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     →     +
        """.trimIndent()
        )
        val robot = model.robots[0]
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +   →       +
        """.trimIndent()
        )

        val result = model.resolveActionCard(robot.id, ActionCard.MoveForward(-1, 0))

        assertEquals(
            expectedModel,
            result.gameModel
        )
        assertEquals(
            listOf(MovementStep(robot.id to Pos(1, 0))),
            result.steps
        )
    }

    @Test
    fun `when the robot moves over the next checkpoint, The checkpoint is registered as taken`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +   → 1     +
        """.trimIndent()
        )
        val robot = model.robots.first()
        val player = model.players.first()
        val checkpoint = model.checkpoints.first()
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +     1 →   +
        """.trimIndent()
        ).copy(players = listOf(player.copy(completedCheckpoints = listOf(checkpoint.id))))

        val result = model.resolveActionCard(robot.id, ActionCard.MoveForward(2, 0))

        assertEquals(
            expectedModel,
            result.gameModel,
        )
        assertEquals(
            listOf(
                MovementStep(
                    MovementPart.Move(robot.id, Pos(2, 0)),
                    MovementPart.TakeCheckpoint(player.id, checkpoint.id)
                ),
                MovementStep(robot.id to Pos(3, 0))
            ),
            result.steps,
        )
    }

    @Test
    fun `when the robot is pushed over the next checkpoint, The checkpoint is registered as taken`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → ↓ 1     +
        """.trimIndent()
        )
        val (r1, r2) = model.robots
        val (p1, p2) = model.players
        val (checkpoint) = model.checkpoints
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +   → ↓     +
        """.trimIndent()).copy(
            checkpoints = model.checkpoints,
            players = listOf(
                p1, p2.copy(completedCheckpoints = listOf(checkpoint.id))
            )
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(1, 0))

        assertEquals(
            expectedModel,
            result.gameModel,
        )
        assertEquals(
            listOf(
                MovementStep(
                    MovementPart.Move(r1.id, Pos(1, 0)),
                    MovementPart.Move(r2.id, Pos(2, 0)),
                    MovementPart.TakeCheckpoint(p2.id, checkpoint.id)
                ),
            ),
            result.steps,
        )
    }

    @Test
    fun `when the robot moves over the next next checkpoint, The checkpoint is not registered as taken`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → 2   1   +
        """.trimIndent()
        )
        val (r1) = model.robots
        val (p1) = model.players
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +   2 → 1   +
        """.trimIndent()).copy(
            players = listOf(p1)
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(2, 0))

        assertEquals(
            expectedModel,
            result.gameModel,
        )
        assertEquals(
            listOf(
                MovementStep(MovementPart.Move(r1.id, Pos(1, 0))),
                MovementStep(MovementPart.Move(r1.id, Pos(2, 0))),
            ),
            result.steps,
        )
    }

    @Test
    @Ignore("Not implemented")
    fun `when the robot moves over the multiple checkpoints in correct order, The checkpoints are all registered as taken`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → 1 2     +
        """.trimIndent()
        )
        val (r1) = model.robots
        val (p1) = model.players
        val (c1, c2) = model.checkpoints
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +   1 2 →   +
        """.trimIndent()).copy(
            players = listOf(
                p1.copy(completedCheckpoints = listOf(c1.id, c2.id))
            )
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(3, 0))

        assertEquals(
            expectedModel,
            result.gameModel,
        )
        assertEquals(
            listOf(
                MovementStep(
                    MovementPart.Move(r1.id, Pos(1, 0)),
                    MovementPart.TakeCheckpoint(p1.id, c1.id),
                ),
                MovementStep(
                    MovementPart.Move(r1.id, Pos(2, 0)),
                    MovementPart.TakeCheckpoint(p1.id, c2.id),
                ),
                MovementStep(
                    MovementPart.Move(r1.id, Pos(3, 0)),
                ),
            ),
            result.steps,
        )
    }
}
