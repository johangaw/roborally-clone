package gamemodel

fun GameModel.dealActionCards(): DealActionCardResult {
    val availableCards = actionDrawPile + actionDiscardPile.shuffled()
    val hands = players.runningFold(0) {start, player -> start + getRobot(player.id).health - 1 }
        .zipWithNext()
        .map { (from, to) -> availableCards.subList(from, to) }
        .zip(players)
        .associate { (cards, player) -> player.id to cards }

    return DealActionCardResult(
        copy(
            players = players.map { it.copy(hand = hands.getValue(it.id)) },
            actionDrawPile = availableCards.drop(hands.values.sumOf { it.size }),
            actionDiscardPile = emptyList()
        ),
        hands
    )
}

data class DealActionCardResult(val gameModel: GameModel, val hands: Map<PlayerId, List<ActionCard>>)
