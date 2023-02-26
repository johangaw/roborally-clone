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
                Pos(3, 0)
            ), result
        )
    }
}
