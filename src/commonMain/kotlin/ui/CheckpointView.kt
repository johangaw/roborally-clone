package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*

const val checkpointViewSize = 100.0

class CheckpointView(checkpointId: CheckpointId, bitmapCache: BitmapCache) : Container() {
    init {
        roundRect(checkpointViewSize, checkpointViewSize, 0.0, fill = Colors.TRANSPARENT_WHITE) {
            val image = image(bitmapCache.checkpoint) {
                val imageSize = checkpointViewSize * 0.8
                size(imageSize, imageSize)
                centerOn(parent!!)
            }
            text(checkpointId.value.toString()) {
                fontSize = 30.0
                color = Colors.WHITE
                centerOn(image)
                alignTopToTopOf(image, checkpointViewSize * 0.09)
            }
        }
    }
}

fun Container.checkpointView(checkpointId: CheckpointId, bitmapCache: BitmapCache, callback: CheckpointView.() -> Unit) =
    CheckpointView(checkpointId, bitmapCache)
        .addTo(this)
        .apply(callback)
