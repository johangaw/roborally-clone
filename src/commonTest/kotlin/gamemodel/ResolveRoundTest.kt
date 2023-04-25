package gamemodel

import gamemodel.RoundResolution.*
import kotlin.test.*

class ResolveRoundTest {

    @Test
    fun `resolve one entire round`() {
        val r1Cards = listOf(ActionCard.MoveForward(1, 10), ActionCard.MoveForward(1, 2))
        val r2Cards = listOf(ActionCard.MoveForward(1, 11), ActionCard.MoveForward(2, 1))
        val cardsPerPlayer = 7  // Both robots will have received 2 points of damage after this round

        val model = gameModel(
            """
            +|+|+|+|+|+|+|+
            +|→       ←|1 +
        """.trimIndent(),
            9 * 2
        ).let {
            val (p1, p2) = it.players
            it.dealCards(
                p1.id to r1Cards,
                p2.id to r2Cards,
            )
        }
        val expectedModel = gameModel(
            """
            +|+|+|+|+|+|+|+
            +|  → ←    |1 +
        """.trimIndent(),
            9 * 2
        ).let {
            val (r1, r2) = it.robots
            val (p1, p2) = it.players
            it.copy(
                robots = listOf(
                    r1.copy(health = 8, registers = emptySet()),
                    r2.copy(health = 8, registers = emptySet()),
                ),
                actionDrawPile = anyOrderList(*(it.actionDrawPile.drop(cardsPerPlayer * 2) + r1Cards + r2Cards).toTypedArray()),
                actionDiscardPile = emptyList(),
                players = listOf(
                    p1.copy(hand = it.actionDrawPile.take(cardsPerPlayer)),
                    p2.copy(
                        hand = it.actionDrawPile
                            .drop(cardsPerPlayer)
                            .take(cardsPerPlayer)
                    ),
                )
            )
        }

        val (r1, r2) = model.robots
        val (p1, p2) = model.players
        val programming = mapOf<PlayerId, List<ActionCard>>(
            p1.id to r1Cards,
            p2.id to r2Cards,
        )

        val result = model.resolveRound(programming)

        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(
                ActionCardMovementResolution(
                    MovementStep(
                        r1.id to Pos(1, 0)
                    )
                ),
                ActionCardMovementResolution(
                    MovementStep(r2.id to Pos(3, 0))
                ),
                ConveyorBeltsResolution(emptyMap(), emptyMap()),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath(listOf(Pos(2, 0), Pos(3, 0)), LaserDirection.Right),
                        LaserPath(listOf(Pos(2, 0), Pos(1, 0)), LaserDirection.Left),
                    ),
                    remainingHealthOfDamagedRobots = mapOf(r1.id to 9, r2.id to 9),
                ),
                CheckpointResolution(emptyMap()),
                ActionCardMovementResolution(
                    MovementStep(r2.id to Pos(2, 0)), MovementStep(
                        r2.id to Pos(1, 0),
                        r1.id to Pos(0, 0),
                    )
                ),
                ActionCardMovementResolution(
                    MovementStep(
                        r1.id to Pos(1, 0),
                        r2.id to Pos(2, 0),
                    )
                ),
                ConveyorBeltsResolution(emptyMap(), emptyMap()),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath(listOf(Pos(2, 0)), LaserDirection.Right),
                        LaserPath(listOf(Pos(1, 0)), LaserDirection.Left),
                    ),
                    remainingHealthOfDamagedRobots = mapOf(r1.id to 8, r2.id to 8),
                ),
                CheckpointResolution(emptyMap()),
                SpawnedRobotsResolution(emptyList()),
                RegisterLockingResolution(emptyMap()),
                WipeRegistersResolution(emptyMap()),
                DealCardsResolution(
                    mapOf(
                        p1.id to model.actionDrawPile.take(cardsPerPlayer),
                        p2.id to model.actionDrawPile
                            .drop(cardsPerPlayer)
                            .take(cardsPerPlayer),
                    )
                ),
            ),
            result.resolutions,
        )
    }

    @Test
    fun `when a robot end the register phase on a checkpoint, it captures that checkpoint`() {
        val card = ActionCard.MoveForward(2, 10)
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →   1     +
        """.trimIndent()
        ).let {
            val (p1) = it.players
            it.copy(
                players = listOf(p1.copy(hand = p1.hand + card))
            )
        }
        val (r1) = model.robots
        val (p1) = model.players
        val (c1) = model.checkpoints()
        val programming = mapOf<PlayerId, List<ActionCard>>(
            p1.id to listOf(card),
        )
        val expectedModel = model.copy(
            robots = listOf(r1.copy(pos = Pos(2, 0), registers = setOf(Register(card, 0, false)))),
            players = listOf(p1.copy(capturedCheckpoints = listOf(c1.id), hand = p1.hand - card))
        )

        val result = model.resolveRound(programming)

        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            listOf(
                ActionCardMovementResolution(
                    MovementStep(r1.id to Pos(1, 0)),
                    MovementStep(r1.id to Pos(2, 0)),
                ),
                ConveyorBeltsResolution(emptyMap(), emptyMap()),
                LaserResolution(
                    laserPaths = setOf(
                        LaserPath((1..101).map { Pos(2 + it, 0) }, LaserDirection.Right),
                    ),
                    remainingHealthOfDamagedRobots = emptyMap(),
                ),
                CheckpointResolution(mapOf(p1.id to c1.id)), WinnerResolution(p1.id),
            ), result.resolutions
        )
    }
}


