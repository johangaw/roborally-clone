package gamemodel

import gamemodel.ActionCardResolutionStep.*
import gamemodel.RoundResolution.*
import kotlin.test.*

class ResolveRoundTest {

    @Test
    fun `when two robots are programmed they are moved accordingly`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +|→       ←|+
        """.trimIndent()
        )
        val (r1, r2) = model.robots
        val (p1, p2) = model.players
        val programming = mapOf<PlayerId, List<ActionCard>>(
            p1.id to listOf(ActionCard.MoveForward(1, 10), ActionCard.MoveForward(1, 2)),
            p2.id to listOf(ActionCard.MoveForward(1, 11), ActionCard.MoveForward(2, 1)),
        )

        val result = model.resolveRound(programming)

        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+
            +|  → ←    |+
        """.trimIndent()
        ).let {
            val (r1, r2) = it.robots
            it.copy(
                robots = listOf(r1.copy(health = 8), r2.copy(health = 8))
            )
        }
        assertEquals(result.gameModel, expectedModel)

        assertEquals(
            listOf(
                ActionCardResolution(
                    MovementStep(
                        r1.id to Pos(1, 0)
                    )
                ),
                ActionCardResolution(
                    MovementStep(r2.id to Pos(3, 0))
                ),
                CheckpointResolution(emptyMap()),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath(listOf(Pos(2, 0), Pos(3, 0)),LaserDirection.Right),
                        LaserPath(listOf(Pos(2, 0), Pos(1, 0)),LaserDirection.Left),
                    ),
                    damage = mapOf(r1.id to 1, r2.id to 1)
                ),
                ActionCardResolution(
                    MovementStep(r2.id to Pos(2, 0)),
                    MovementStep(
                        r2.id to Pos(1, 0),
                        r1.id to Pos(0, 0),
                    )
                ),
                ActionCardResolution(
                    MovementStep(
                        r1.id to Pos(1, 0),
                        r2.id to Pos(2, 0),
                    )
                ),
                CheckpointResolution(emptyMap()),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath(listOf(Pos(2, 0)),LaserDirection.Right),
                        LaserPath(listOf(Pos(1, 0)),LaserDirection.Left),
                    ),
                    damage = mapOf(r1.id to 1, r2.id to 1)
                ),
            ),
            result.resolutions
        )
    }

    @Test
    fun `when a robot end the register phase on a checkpoint, it captures that checkpoint`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   1     +
        """.trimIndent()
        )
        val (r1) = model.robots
        val (p1) = model.players
        val (c1) = model.checkpoints
        val programming = mapOf<PlayerId, List<ActionCard>>(
            p1.id to listOf(ActionCard.MoveForward(2, 10)),
        )
        val expectedModel = model.copy(
            robots = listOf(r1.copy(pos = Pos(2, 0))),
            players = listOf(p1.copy(capturedCheckpoints = listOf(c1.id)))
        )

        val result = model.resolveRound(programming)

        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(
                ActionCardResolution(
                    MovementStep(r1.id to Pos(1, 0)),
                    MovementStep(r1.id to Pos(2, 0)),
                ),
                CheckpointResolution(mapOf(p1.id to c1.id)),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath((1..101).map { Pos(2 + it, 0) }, LaserDirection.Right),
                    ),
                    damage = emptyMap()
                )

            ),
            result.resolutions
        )
    }
}


