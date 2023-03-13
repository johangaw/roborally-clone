package gamemodel

sealed class ActionCard {

    abstract val initiative: Int
    data class MoveForward(val distance: Int, override val initiative: Int) : ActionCard()
    data class Turn(val type: gamemodel.Turn, override val initiative: Int): ActionCard()
}

enum class Turn(val degrees: Int) {
    Right(90),
    Left(-90),
    UTurn(180),
}


val move1Initiative = listOf(490, 500, 510, 520, 530, 540, 550, 560, 570, 580, 590, 600, 610, 620, 630, 640, 650, 660)
val move2Initiative = listOf(670, 680, 690, 700, 710, 720, 730, 740, 750, 760, 770, 780)
val move3Initiative = listOf(790, 800, 810, 820, 830, 840)
val moveBackwardsInitiative = listOf(430, 440, 450, 460, 470, 480)
val turnHalfTurnInitiative = arrayOf(10, 20, 30, 40, 50, 60)
val turnRightInitiative = arrayOf(80, 100, 120, 140, 160, 180, 200, 220, 240, 260, 280, 300, 320, 340, 360, 380, 400, 420)
val turnLeftInitiative = arrayOf(70, 90, 110, 130, 150, 170, 190, 210, 230, 250, 270, 290, 310, 330, 350, 370, 390, 410)

fun actionCardDeck(): List<ActionCard> = listOf(
    move1Initiative.map { ActionCard.MoveForward(1, it) },
    move2Initiative.map { ActionCard.MoveForward(2, it) },
    move3Initiative.map { ActionCard.MoveForward(3, it) },
    moveBackwardsInitiative.map { ActionCard.MoveForward(-1, it) },
    turnRightInitiative.map { ActionCard.Turn(Turn.Right, it) },
    turnLeftInitiative.map { ActionCard.Turn(Turn.Left, it) },
    turnHalfTurnInitiative.map { ActionCard.Turn(Turn.UTurn, it) },
).flatten()




