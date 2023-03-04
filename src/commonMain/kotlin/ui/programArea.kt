package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import kotlin.math.*

fun Container.programArea(cellSize: Double, callback: @ViewDslMarker() (RoundRect.() -> Unit) = {}): View {

    val programingSlotWidth = cellSize
    val programingSlotHeight = cellSize * 1.5
    val programingSlotPadding = 10.0

    var selectedCards = mapOf<Int, Int>()

    return roundRect(800.0, 200.0, 0.0) {
        val programmingSlots = (0..4).map {
            roundRect(programingSlotWidth, programingSlotHeight, 3.0) {
                alignBottomToBottomOf(parent!!)
                alignLeftToLeftOf(parent!!, programingSlotPadding)
                fill = Colors.LIGHTGRAY
            }
        }

        programmingSlots.windowed(2, 1).forEach { (left, right) ->
            right.alignLeftToRightOf(left, programingSlotPadding)
        }

        val cardWidth = programingSlotWidth / 2
        val cardHeight = programingSlotHeight / 2
        val cardPadding = programingSlotPadding / 2
        val cards = (0..11).associate {
            it to roundRect(cardWidth, cardHeight, 3.0) {
                alignTopToTopOf(parent!!,cardPadding)
                alignRightToRightOf(parent!!, programingSlotPadding)
                fill = Colors.BLUE
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

        cards.forEach {(cardIndex, card) ->
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
                    programmingSlots.withIndex().firstOrNull { (_, slot) ->
                        card.collidesWith(slot)
                    }?.let { (slotIndex, slot) ->
                        card.centerOn(slot)
                        dragOriginalPos = card.pos
                        selectedCards = selectedCards + (slotIndex to cardIndex)
                    } ?: run {
                        card.pos = originalPos
                        card.scale = 1.0
                        dragOriginalPos = originalPos
                        selectedCards = selectedCards.filterValues { it != cardIndex }
                    }
                }
            }
        }

        this.callback()
    }
}
