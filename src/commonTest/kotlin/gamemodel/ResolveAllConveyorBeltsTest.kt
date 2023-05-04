package gamemodel

import kotlin.test.*

class ResolveAllConveyorBeltsTest {

    @Test
    fun `when no robot is on a conveyor belt, nothing happens`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )

        val result = model.resolveAllConveyorBelts()
        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
    }

    @Test
    fun `when a robot stands on a conveyor belt, it is moved one step in the direction of the belt`() {
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
        val (r1) = model.robots
        val expectedModel = model.mapRobot(r1.id) { it.copy(pos = Pos(1, 0)) }

        val result = model.resolveAllConveyorBelts()
        assertEquals(expectedModel, result.gameModel)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(mapOf(r1.id to Pos(1, 0)), result.movedRobots)
    }

    @Test
    fun `when a robot is moved onto conveyor belt curve on a conveyor belt, it is also rotated accordingly to the curve`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ←         +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                    Pos(1, 0) to ConveyorBelt(ConveyorBeltType.RightAndDown, ConveyorBeltSpeed.Regular),
                )
            )
        }
        val (r1) = model.robots
        val expectedModel = model.mapRobot(r1.id) { it.copy(pos = Pos(1, 0), dir = Direction.Up) }

        val result = model.resolveAllConveyorBelts()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(mapOf(r1.id to Pos(1, 0)), result.movedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(mapOf(r1.id to Direction.Up), result.rotatedRobots)
    }

    @Test
    fun `when a another robot not on the same belt is blocking the way, it is not moved (conveyor belt does not push other robots)`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + ← ↑       +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                )
            )
        }

        val result = model.resolveAllConveyorBelts()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
    }

    @Test
    fun `when a another robot is standing one step ahead on the same belt, both of them are moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → ↑       +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                    Pos(1, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                )
            )
        }
        val (r1, r2) = model.robots
        val expectedModel = model.copy(
            robots = listOf(r1.copy(pos = Pos(1, 0)), r2.copy(pos = Pos(2, 0)))
        )

        val result = model.resolveAllConveyorBelts()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            mapOf(
                r1.id to Pos(1, 0),
                r2.id to Pos(2, 0),
            ),
            result.movedRobots,
        )
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
    }

    @Test
    fun `when two belts tries to move two robots to the same position, none of the robots are moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   ←     +
        """.trimIndent()
        ).mapCourse { _, course ->
            course.copy(
                conveyorBelts = mapOf(
                    Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                    Pos(2, 0) to ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular),
                )
            )
        }

        val result = model.resolveAllConveyorBelts()

        assertEquals(model, result.gameModel)
        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
    }

    @Test
    fun `when a belt tries two robots through each other, none of them are moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → ←       +
        """.trimIndent()
        ).addConveyorBelts {
            mapOf(
                Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                Pos(1, 0) to ConveyorBelt(ConveyorBeltType.Left, ConveyorBeltSpeed.Regular),
            )
        }

        val result = model.resolveAllConveyorBelts()
        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(model, result.gameModel)
    }

    @Test
    fun `when a robot is blocked by a chain of robots, none of them is moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + → → → →   +
        """.trimIndent()
        ).addConveyorBelts {
            mapOf(
                Pos(0, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                Pos(1, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
                Pos(2, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
            )
        }

        val result = model.resolveAllConveyorBelts()

        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(model, result.gameModel)
    }

    @Test
    fun `when a robot is blocked by a wall, it is not moved`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +   →|      +
        """.trimIndent()
        ).addConveyorBelts {
            mapOf(
                Pos(1, 0) to ConveyorBelt(ConveyorBeltType.Right, ConveyorBeltSpeed.Regular),
            )
        }

        val result = model.resolveAllConveyorBelts()

        assertEquals(emptyMap(), result.movedRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(emptyMap(), result.remainingHealthOfFallenRobots)
        assertEquals(model, result.gameModel)
    }

    @Test
    fun `when a robot is moved off the course by an conveyor belt, it is destroyed`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     →     +
        """.trimIndent()
        ).addConveyorBelts {
            mapOf(
                Pos(2, 0) to ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular),
            )
        }
        val (r1) = model.robots
        val expectedModel = model.copy(
            robots = emptyList(),
            destroyedRobots = listOf(r1.copy(pos = Pos(2, -1), health = 8))
        )

        val result = model.resolveAllConveyorBelts()

        assertEquals(mapOf(r1.id to Pos(2, -1)), result.movedRobots)
        assertEquals(emptyMap(), result.rotatedRobots)
        assertEquals(mapOf(r1.id to 8), result.remainingHealthOfFallenRobots)
        assertEquals(expectedModel, result.gameModel)
    }
}
