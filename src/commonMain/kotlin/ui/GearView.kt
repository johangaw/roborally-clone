package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import gamemodel.*

private val baseSize = 128.0

class GearView(rotation: Rotation, bitmapCache: BitmapCache) : Container() {

    init {
        roundRect(baseSize, baseSize, 0.0, fill = Colors.TRANSPARENT_WHITE) {
            val bitmap = when(rotation) {
                Rotation.None -> throw IllegalArgumentException("This should never happen")
                Rotation.Clockwise -> bitmapCache.gearClockwise
                Rotation.CounterClockwise -> bitmapCache.gearCounterClockwise
            }
            image(bitmap) {
                size(baseSize, baseSize)
                centerOn(parent!!)
            }
        }
    }
}

fun Container.gearView(rotation: Rotation, bitmapCache: BitmapCache, callback: GearView.() -> Unit = {}) =
    GearView(rotation, bitmapCache)
        .addTo(this)
        .apply(callback)
