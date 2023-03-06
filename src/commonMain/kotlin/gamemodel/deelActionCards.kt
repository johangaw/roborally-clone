package gamemodel

fun GameModel.dealActionCards(): DealActionCardResult {
    val cardsPerPlayer = 11 // TODO this should change with the robots damage
    val cardsToDeal = players.size * cardsPerPlayer

    if (cardsToDeal > actionDrawPile.size) return shuffleActionCards().dealActionCards()

    val hands = players.mapIndexed { index: Int, player: Player ->
        player.id to actionDrawPile.subList(index * cardsPerPlayer, index * cardsPerPlayer + cardsPerPlayer)
    }.toMap()

    return DealActionCardResult(
        copy(
            players = players.map { it.copy(hand = hands.getValue(it.id)) },
            actionDrawPile = actionDrawPile.drop(hands.values.sumOf { it.size }),
            actionDiscardPile = actionDiscardPile + actionDrawPile.take(hands.values.sumOf { it.size })
        ),
        hands
    )
}

private fun GameModel.shuffleActionCards() =
    copy(actionDrawPile = actionDiscardPile + actionDiscardPile.shuffled(), actionDiscardPile = emptyList())

data class DealActionCardResult(val gameModel: GameModel, val hands: Map<PlayerId, List<ActionCard>>)
