package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*
import kotlin.math.*

class ProgramArea(cellSize: Double) : Container() {

    private val programingSlotWidth = cellSize
    private val programingSlotHeight = cellSize * 1.5
    private val programingSlotPadding = 10.0

    private val cardWidth = programingSlotWidth / 2
    private val cardHeight = programingSlotHeight / 2
    private val cardPadding = programingSlotPadding / 2

    private var cards = mapOf<ActionCard, RoundRect>()
    private lateinit var programmingSlots: Map<Int, RoundRect>

    var selectedCards = mapOf<Int, ActionCard>()
        private set

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
        cards.values.forEach {
            it.removeFromParent()
        }
        cards = emptyMap()
    }

    fun dealCards(newCards: List<ActionCard>) {
        clearCards()

        cards = newCards.associate {cardModel ->
            cardModel to roundRect(cardWidth, cardHeight, 3.0) {
                alignTopToTopOf(parent!!,cardPadding)
                alignRightToRightOf(parent!!, programingSlotPadding)
                fill = Colors.BLUE

                if( cardModel is ActionCard.MoveForward ) {
                    text(cardModel.distance.toString()) {
                        centerOn(this@roundRect)
                    }
                }

            }
        }

        val (upperRow, lowerRow) = cards.values.chunked(ceil(cards.size / 2.0).toInt())

        upperRow.windowed(2, 1).forEach { (first, second) ->
            second.alignRightToLeftOf(first, cardPadding)
        }

        lowerRow.windowed(2, 1).forEach { (first, second) ->
            second.alignRightToLeftOf(first, cardPadding)
            second.alignTopToBottomOf(upperRow.first(), cardPadding)
        }

        cards.forEach {(cardModel, card) ->
            val originalPos = card.pos
            var dragOriginalPos = originalPos
            card.onMouseDrag {
                if(it.start) {
                    card.scale = 2.0
                    card.zIndex = 1.0
                }

                card.x = dragOriginalPos.x + it.dx
                card.y = dragOriginalPos.y + it.dy

                if(it.end) {
                    card.zIndex = 0.0
                    programmingSlots.entries.firstOrNull { (_, slot) ->
                        card.collidesWith(slot)
                    }?.let { (slotIndex, slot) ->
                        card.centerOn(slot)
                        dragOriginalPos = card.pos
                        selectedCards = selectedCards + (slotIndex to cardModel)
                    } ?: run {
                        card.pos = originalPos
                        card.scale = 1.0
                        dragOriginalPos = originalPos
                        selectedCards = selectedCards.filterValues { it != cardModel }
                    }
                }
            }
        }
    }
}

fun Container.programArea(cellSize: Double, callback: @ViewDslMarker() (ProgramArea.() -> Unit) = {}) =
    ProgramArea(cellSize).addTo(this, callback)
