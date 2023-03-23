package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
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

suspend fun Container.card(
    actionCard: ActionCard,
    cardWidth: Double,
    cardHeight: Double,
    callback: @ViewDslMarker() (Card.() -> Unit) = {}
): Card {
    val bitmap = when (actionCard) {
        is ActionCard.MoveForward -> when (actionCard.distance) {
            1 -> resourcesVfs["forward1.png"].readBitmap()
            2 -> resourcesVfs["forward2.png"].readBitmap()
            3 -> resourcesVfs["forward3.png"].readBitmap()
            -1 -> resourcesVfs["back_up.png"].readBitmap()
            else -> throw IllegalArgumentException("Unable to find a bitmap for card $actionCard")
        }

        is ActionCard.Turn -> when(actionCard.type) {
            Turn.Right -> resourcesVfs["right.png"].readBitmap()
            Turn.Left -> resourcesVfs["left.png"].readBitmap()
            Turn.UTurn -> resourcesVfs["u_turn.png"].readBitmap()
        }
    }

    return Card(actionCard, cardWidth, cardHeight, bitmap).addTo(this, callback)
}
