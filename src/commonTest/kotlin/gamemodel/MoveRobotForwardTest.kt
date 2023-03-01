package gamemodel

import kotlin.test.*

class MoveRobotForwardTest {

    @Test
    fun `when there is nothing blocking the robot it is moved`() {
        val model = gameModel(
            """
            →____
        """.trimIndent()
        )
        val robot = model.robots.first()
        val expectedModel = gameModel(
            """
            ___→_
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
    fun `when another robot blocks the way it is push along`() {
        val model = gameModel(
            """
            →_↓__
        """.trimIndent()
        )
        val pusher = model.robots[0]
        val pushed = model.robots[1]
        val expectedModel = gameModel(
            """
            ___→↓
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
}
