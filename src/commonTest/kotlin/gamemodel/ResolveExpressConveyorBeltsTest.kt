package gamemodel

import kotlin.test.*

class ResolveExpressConveyorBeltsTest {

    @Test
    fun `when a robot stands on a regular conveyor belt, it is not moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular)
                )
            )
        }

        val result = model.resolveExpressConveyorBelts()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(emptyMap(), result.movedRobots)
    }


    @Test
    fun `when a robot stands on an express conveyor belt, it is moved one steps along the belt`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Express),
                    Pos(1, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Express),
                    Pos(2, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Express),
                )
            )
        }
        val (r1) = model.robots
        val expectedModel = model.mapRobot(r1.id) { it.copy(pos = Pos(1, 0)) }

        val result = model.resolveExpressConveyorBelts()
        assertEquals(expectedModel, result.gameModel)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(mapOf(r1.id to Pos(1, 0)), result.movedRobots)
    }
}
