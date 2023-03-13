package gamemodel

import org.junit.Test
import kotlin.test.*

class RotateRobotTest {

    @Test
    fun `when a robot is facing up and turns right, it will be facing right`() = validateRotation(
        Direction.Up, Turn.Right, Direction.Right
    )

    @Test
    fun `when a robot is facing up and turns left, it will be facing left`() = validateRotation(
        Direction.Up, Turn.Left, Direction.Left
    )

    @Test
    fun `when a robot is facing down and turns left, it will be facing right`() = validateRotation(
        Direction.Up, Turn.Left, Direction.Left
    )

    @Test
    fun `when a robot is facing left and makes a u-turn, it will be facing right`() = validateRotation(
        Direction.Left, Turn.UTurn, Direction.Right
    )


    private fun validateRotation(originalDirection: Direction, rot: Turn, expectedDirection: Direction) {
        val (r1, model) = gameModel("""
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()).let { model ->
            val robot = model.robots.first()
            robot to model.mapRobot(robot.id) { it.copy(dir = originalDirection) }
        }

        val result = model.controlRobot(r1.id, ActionCard.Turn(rot, 0))

        assertEquals(
            RobotActionResult.Turned(
                model.mapRobot(r1.id) { it.copy(dir = expectedDirection) },
                r1.id,
                expectedDirection
            ),
            result
        )
    }
}
