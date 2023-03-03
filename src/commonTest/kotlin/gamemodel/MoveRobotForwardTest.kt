package gamemodel

import kotlin.test.*

class MoveRobotForwardTest {

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

        val result = model.controlRobot(robot.id, ActionCard.MoveForward(3))

        assertEquals(
            RobotActionResult.Moved(
                expectedModel,
                listOf(
                    mapOf(robot.id to Pos(1, 0)),
                    mapOf(robot.id to Pos(2, 0)),
                    mapOf(robot.id to Pos(3, 0))
                )
            ), result
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

        val result = model.controlRobot(robot.id, ActionCard.MoveForward(3))

        assertEquals(
            RobotActionResult.Moved(
                expectedModel,
                listOf(
                    mapOf(robot.id to Pos(2, 0)),
                )
            ), result
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

        val result = model.controlRobot(pusher.id, ActionCard.MoveForward(3))

        assertEquals(
            RobotActionResult.Moved(
                expectedModel,
                listOf(
                    mapOf(pusher.id to Pos(1, 0)),
                    mapOf(pusher.id to Pos(2, 0), pushed.id to Pos(3, 0)),
                    mapOf(pusher.id to Pos(3, 0), pushed.id to Pos(4, 0))
                )
            ), result
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

        val result = model.controlRobot(pusher.id, ActionCard.MoveForward(3))

        assertEquals(
            RobotActionResult.Moved(
                expectedModel,
                listOf(
                    mapOf(pusher.id to Pos(1, 0)),
                    mapOf(pusher.id to Pos(2, 0), pushed.id to Pos(3, 0)),
                )
            ), result
        )
    }
}
