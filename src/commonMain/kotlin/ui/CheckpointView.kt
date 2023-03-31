package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*

const val checkpointViewSize = 100.0

class CheckpointView(checkpointOrder: Int, bitmapCache: BitmapCache) : Container() {
    init {
        val image = image(bitmapCache.checkpoint) {
            val imageSize = checkpointViewSize
            size(imageSize, imageSize)
            centerOn(parent!!)
        }

        text(checkpointOrder.toString()) {
            fontSize = 30.0
            color = Colors.WHITE
            centerOn(image)
            alignTopToTopOf(image, checkpointViewSize * 0.16)
        }
    }
}

fun Container.checkpointView(checkpointOrder: Int, bitmapCache: BitmapCache, callback: CheckpointView.() -> Unit) =
    CheckpointView(checkpointOrder, bitmapCache).addTo(this).apply(callback)
