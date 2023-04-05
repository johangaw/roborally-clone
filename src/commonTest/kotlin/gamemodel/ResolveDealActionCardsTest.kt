package gamemodel

import org.junit.Test
import kotlin.test.*

class ResolveDealActionCardsTest {

    @Test
    fun `when all robots have all their health left, it deals 9 cards to each`() {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     ↓ ↓   +
        """.trimIndent(),
            null
        )
        val originalDrawPile = model.actionDrawPile

        val result = model.resolveDealActionCards()

        val (p1, p2) = result.gameModel.players
        assertEquals(9, p1.hand.size)
        assertEquals(9, p2.hand.size)
        assertEquals(originalDrawPile.size - 9 - 9, result.gameModel.actionDrawPile.size)
        assertEquals(0, result.gameModel.actionDiscardPile.size)
    }

    @Test
    fun `when a robot has 9 health, it receives 8 cards`() = hasReceivedCorrectHand(9, 8)

    @Test
    fun `when a robot has 5 health, it receives 4 cards`() = hasReceivedCorrectHand(5, 4)

    @Test
    fun `when a robot has 1 health, it receives 0 cards`() = hasReceivedCorrectHand(9, 8)

    private fun hasReceivedCorrectHand(health: Int, expectedHandSize: Int) {
        val model = gameModel(
            """
            +|+|+|+|+|+|+
            +     ↓     +
        """.trimIndent(),
            null
        ).let { model -> model.copy(robots = model.robots.map { it.copy(health = health) }) }

        val result = model.resolveDealActionCards()

        assertEquals(expectedHandSize, result.gameModel.players.first().hand.size)
        assertEquals(expectedHandSize, result.hands.values.first().size)
    }
}
