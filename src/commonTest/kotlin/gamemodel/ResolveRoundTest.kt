package gamemodel

import kotlin.test.*

class ResolveRoundTest {

    @Test
    fun `when two robots are programmed they are moved accordingly`() {
        val model = gameModel("""
            +|+|+|+|+|+|+
            +|→       ←|+
        """.trimIndent())
        val (r1, r2) = model.robots
        val (p1, p2) = model.players
        val programming = mapOf<PlayerId, List<ActionCard>>(
            p1.id to listOf(ActionCard.MoveForward(1, 10), ActionCard.MoveForward(1, 2)),
            p2.id to listOf(ActionCard.MoveForward(1, 11), ActionCard.MoveForward(2, 1)),
        )


        val result = model.resolveRound(programming)

        val expectedModel = gameModel("""
            +|+|+|+|+|+|+
            +|  → ←    |+
        """.trimIndent())
        assertEquals(result.gameModel, expectedModel)
        assertEquals(result.steps, listOf(
            ResolutionStep.MoveRobot(listOf(mapOf(r1.id to Pos(1, 0)))),
            ResolutionStep.MoveRobot(listOf(mapOf(r2.id to Pos(3, 0)))),
            ResolutionStep.MoveRobot(listOf(mapOf(r2.id to Pos(2, 0)), mapOf(r2.id to Pos(1, 0), r1.id to Pos(0 ,0)))),
            ResolutionStep.MoveRobot(listOf(mapOf(r1.id to Pos(1, 0), r2.id to Pos(2 ,0)))),
        ))
    }
}
