package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import gamemodel.*


class Card(
    val actionCard: ActionCard,
    cardWidth: Double,
    cardHeight: Double,
    bitmap: Bitmap?
) : Container() {

    private var _onDrop: Card.(info: MouseDragInfo) -> Unit = {}
    private var pickupPos = pos
    private var originalPos = pos

    var draggable = true

    init {
        if (bitmap == null) {
            roundRect(cardWidth, cardHeight, 3.0) {
                fill = Colors.BLUE
                if (actionCard is ActionCard.MoveForward) {
                    text(actionCard.distance.toString()) {
                        centerOn(this@roundRect)
                    }
                }
            }
        } else {
            image(bitmap) {
                size(cardWidth, cardHeight)
            }
        }

        var scalingWidthChange = 0.0
        var scalingHeightChange = 0.0
        onMouseDrag {
            if(!draggable) return@onMouseDrag

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

fun Container.card(
    actionCard: ActionCard,
    cardWidth: Double,
    cardHeight: Double,
    bitmapCache: BitmapCache,
    callback: @ViewDslMarker() (Card.() -> Unit) = {}
): Card {
    val bitmap = when (actionCard) {
        is ActionCard.MoveForward -> when (actionCard.distance) {
            1 -> bitmapCache.forward1
            2 -> bitmapCache.forward2
            3 -> bitmapCache.forward3
            -1 -> bitmapCache.backUp
            else -> throw IllegalArgumentException("Unable to find a bitmap for card $actionCard")
        }

        is ActionCard.Turn -> when(actionCard.type) {
            Turn.Right -> bitmapCache.right
            Turn.Left -> bitmapCache.left
            Turn.UTurn -> bitmapCache.uTurn
        }
    }

    return Card(actionCard, cardWidth, cardHeight, bitmap).addTo(this, callback)
}
