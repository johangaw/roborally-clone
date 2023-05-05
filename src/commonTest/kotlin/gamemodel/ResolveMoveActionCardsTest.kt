package gamemodel

import kotlin.test.*

class ResolveMoveActionCardsTest {

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

        assertIsMovementResult(result)

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

        assertIsMovementResult(result)
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
    fun `when a wall is right next to the robot, it does not move through that wall`() {
        val model = gameModel(
            """
        +|+|+|+|+|+
        +    |←   +
        """.trimIndent()
        )
        val robot = model.robots.first()

        val result = model.resolveActionCard(robot.id, ActionCard.MoveForward(3, 0))

        assertIsMovementResult(result)
        assertEquals(model, result.gameModel)
        assertEquals(emptyList(), result.steps)
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

        assertIsMovementResult(result)
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

        assertIsMovementResult(result)
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

        assertIsMovementResult(result)
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
    fun `when a robot moves off the course, it is destroyed`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +         → +
        """.trimIndent()
        )
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = emptyList(),
            destroyedRobots = listOf(r1.copy(pos = Pos(5, 0), health = 8))
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(1, 0))

        assertIsMovementResult(result)
        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(MovementStep(
                RobotMove(r1.id, Pos(5, 0)),
                RobotFall(r1.id, 8),
            )),
            result.steps
        )
    }

    @Test
    fun `when a robot is pushed off the course, it is destroyed`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +       → → +
        """.trimIndent()
        )
        val (r1, r2) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(pos = Pos(4, 0))),
            destroyedRobots = listOf(r2.copy(pos = Pos(5, 0), health = 8))
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(1, 0))

        assertIsMovementResult(result)
        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(
                MovementStep(
                    RobotMove(r1.id, Pos(4, 0)),
                    RobotMove(r2.id, Pos(5, 0)),
                    RobotFall(r2.id, 8),
                ),
            ),
            result.steps
        )
    }

    @Test
    fun `when a robot tries to moves far out off the course, it is only moved one step`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +         → +
        """.trimIndent()
        )
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = emptyList(),
            destroyedRobots = listOf(r1.copy(pos = Pos(5, 0), health = 8))
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(3, 0))

        assertIsMovementResult(result)
        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(
                MovementStep(
                    RobotMove(r1.id, Pos(5, 0)),
                    RobotFall(r1.id, 8),
                ),
            ),
            result.steps
        )
    }

    @Test
    fun `when a robot moves into a pit, it is destroyed`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(pits = setOf(Pos(1,0)))
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = emptyList(),
            destroyedRobots = listOf(r1.copy(pos = Pos(1, 0), health = 8))
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(1, 0))

        assertIsMovementResult(result)
        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(MovementStep(
                RobotMove(r1.id, Pos(1, 0)),
                RobotFall(r1.id, 8),
            )),
            result.steps
        )
    }

    @Test
    fun `when a robot moves tries to move over a pit, it is destroyed and stops its movement in the pit`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(pits = setOf(Pos(1,0)))
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = emptyList(),
            destroyedRobots = listOf(r1.copy(pos = Pos(1, 0), health = 8))
        )

        val result = model.resolveActionCard(r1.id, ActionCard.MoveForward(3, 0))

        assertIsMovementResult(result)
        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(MovementStep(
                RobotMove(r1.id, Pos(1, 0)),
                RobotFall(r1.id, 8),
            )),
            result.steps
        )
    }
}
