package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlin.math.*

class ProgramArea(cellSize: Double, val playerId: PlayerId) : Container() {

    private val programingSlotWidth = cellSize
    private val programingSlotHeight = cellSize * 1.5
    private val programingSlotPadding = 10.0

    private val cardWidth = programingSlotWidth / 2
    private val cardHeight = programingSlotHeight / 2
    private val cardPadding = programingSlotPadding / 2

    private var cards = listOf<Card>()
    private lateinit var programmingSlots: Map<Int, RoundRect>
    private val selectedCards = arrayOf<Card?>(null, null, null, null, null)

    init {
        roundRect(800.0, 200.0, 0.0) {
            programmingSlots = (0..4).associateWith {
                roundRect(programingSlotWidth, programingSlotHeight, 3.0) {
                    alignBottomToBottomOf(parent!!)
                    alignLeftToLeftOf(parent!!, programingSlotPadding)
                    fill = Colors.LIGHTGRAY
                }
            }

            programmingSlots.values.windowed(2, 1).forEach { (left, right) ->
                right.alignLeftToRightOf(left, programingSlotPadding)
            }
        }
    }

    fun clearCards() {
        cards.forEach {
            it.removeFromParent()
        }
        cards = emptyList()
    }

    fun dealCards(newCards: List<ActionCard>) {
        clearCards()

        cards = newCards.map { cardModel ->
            card(cardModel, cardWidth, cardHeight) {
                alignTopToTopOf(parent!!, programingSlotPadding)
                alignRightToRightOf(parent!!, programingSlotPadding)

                onDrop {
                    programmingSlots.entries.firstOrNull { (_, slot) ->
                        collidesWith(slot)
                    }?.let { (slotIndex, slot) ->
                        scale = 2.0
                        centerOn(slot)

                        selectedCards[slotIndex]?.useOriginalPos()
                        selectedCards.remove(this)
                        selectedCards[slotIndex] = this
                    } ?: run {
                        useOriginalPos()
                        selectedCards.remove(this)
                    }

                }
            }
        }

        val (upperRow, lowerRow) = cards.chunked(ceil(cards.size / 2.0).toInt())

        upperRow.windowed(2, 1).forEach { (first, second) ->
            second.alignRightToLeftOf(first, cardPadding)
        }

        lowerRow.first().apply {
            alignTopToBottomOf(upperRow.first(), cardPadding)
        }
        lowerRow.windowed(2, 1).forEach { (first, second) ->
            second.alignRightToLeftOf(first, cardPadding)
            second.alignTopToBottomOf(upperRow.first(), cardPadding)
        }

        cards.forEach { it.storeOriginalPos() }
    }

    fun getSelectedCards(): List<ActionCard> =
        selectedCards.map { it?.actionCard }.filterNotNull()

}

fun Container.programArea(
    cellSize: Double,
    playerId: PlayerId,
    callback: @ViewDslMarker() (ProgramArea.() -> Unit) = {}
) =
    ProgramArea(cellSize, playerId).addTo(this, callback)


private class Card(val actionCard: ActionCard, cardWidth: Double, cardHeight: Double) : Container() {

    private var _onDrop: Card.(info: MouseDragInfo) -> Unit = {}
    private var pickupPos = pos
    private var originalPos = pos


    init {
        roundRect(cardWidth, cardHeight, 3.0) {
            fill = Colors.BLUE
            if (actionCard is ActionCard.MoveForward) {
                text(actionCard.distance.toString()) {
                    centerOn(this@roundRect)
                }
            }
        }

        var scalingWidthChange = 0.0
        var scalingHeightChange = 0.0
        onMouseDrag {
            if (it.start) {
                val newScale = 2.0
                scalingWidthChange = cardWidth * (newScale - scaleX)
                scalingHeightChange = cardHeight * (newScale - scaleY)
                scale = newScale
                zIndex = 1.0
                pickupPos = pos
            }

            val widthScalingCompensation = scalingWidthChange / 2
            val heightScalingCompensation = scalingHeightChange / 2
            x = pickupPos.x + it.dx - widthScalingCompensation
            y = pickupPos.y + it.dy - heightScalingCompensation

            if (it.end) {
                scale = 1.0
                zIndex = 0.0
                _onDrop(it)
            }
        }
    }

    fun onDrop(callback: Card.(info: MouseDragInfo) -> Unit) {
        _onDrop = callback
    }

    fun storeOriginalPos() {
        originalPos = pos
    }

    fun useOriginalPos() {
        pos = originalPos
        scale = 1.0
    }
}

private fun Container.card(
    actionCard: ActionCard,
    cardWidth: Double,
    cardHeight: Double,
    callback: @ViewDslMarker() (Card.() -> Unit) = {}
) =
    Card(actionCard, cardWidth, cardHeight).addTo(this, callback)

fun <T>Array<T?>.remove(item: T) {
    if(contains(item)) {
        set(indexOf(item), null)
    }
}
