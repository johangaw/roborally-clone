package gamemodel

import com.soywiz.korio.file.sync.*
import org.junit.Test
import kotlin.test.*

class ResolveWipeRegistersTest {
    @Test
    fun `when no robots has been programmed, remove all cards the players`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        ).dealCards()
        val (p1) = model.players
        val expectedModel = model.copy(
            players = listOf(p1.copy(hand = emptyList())),
            actionDiscardPile = p1.hand
        )

        val result = model.resolveWipeRegisters()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(emptyMap(), result.lockedRegisters)
    }

    @Test
    fun `when robots have been programed, move those cards the to discard pile`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )
            .dealCards()
            .programAllRobots()
        val (r1) = model.robots
        val (p1) = model.players

        val expectedModel = model.copy(
            robots = listOf(r1.copy(registers = emptySet())),
            players = listOf(p1.copy(hand = emptyList())),
            actionDiscardPile = p1.hand + r1.registers.map { it.card },
        )

        val result = model.resolveWipeRegisters()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(emptyMap(), result.lockedRegisters)
    }

    @Test
    fun `when a robots registers are locked, those cards are not discarded`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            + →         +
        """.trimIndent()
        )
            .dealCards()
            .programAllRobots()
            .let {
                val (r1) = it.robots
                val (p1) = it.players
                val (c0, c1, c2, c3, c4) = it.getRobotCards(r1.id)
                it.copy(
                    robots = listOf(
                        r1.copy(
                            health = 4,
                            registers = setOf(
                                Register(c0, 0, false),
                                Register(c1, 1, false),
                                Register(c2, 2, false),
                                Register(c3, 3, true),
                                Register(c4, 4, true),
                            ),
                        )
                    ),
                    players = listOf(
                        p1.copy(
                            hand = p1.hand - it
                                .getRobotCards(r1.id)
                                .toSet()
                        )
                    )
                )
            }
        val (r1) = model.robots
        val (p1) = model.players
        val (c0, c1, c2, c3, c4) = model.getRobotCards(r1.id)
        val expectedModel = model.copy(
            actionDiscardPile = p1.hand + setOf(c0, c1, c2),
            robots = listOf(
                r1.copy(
                    registers = setOf(
                        Register(c3, 3, true),
                        Register(c4, 4, true),
                    )
                )
            ),
            players = listOf(
                p1.copy(hand = emptyList())
            )
        )

        val result = model.resolveWipeRegisters()

        assertEquals(expectedModel, result.gameModel)
        assertEquals(
            mapOf(
                r1.id to listOf(
                    LockedRegister(3, c3),
                    LockedRegister(4, c4),
                )
            ), result.lockedRegisters
        )
    }
}
