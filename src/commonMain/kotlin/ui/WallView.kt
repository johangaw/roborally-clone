package ui

import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*

class WallView(bitmapCache: BitmapCache, dir: Direction) : Container() {

    private val baseSize: Double = 128.0

    init {
        image(bitmapCache.wall) {
            setSize(baseSize, baseSize)
            val angle = when (dir) {
                Direction.Up -> Angle.ZERO
                Direction.Down -> Angle.HALF
                Direction.Right -> Angle.QUARTER
                Direction.Left -> -Angle.QUARTER
            }

            setTransform(
                Matrix()
                    .translate(-baseSize / 2.0, -baseSize / 2.0)
                    .rotate(angle)
                    .translate(baseSize / 2.0, baseSize / 2.0)
                    .toTransform()
            )
        }
    }
}

fun Container.wallView(bitmapCache: BitmapCache, dir: Direction, callback: WallView.() -> Unit = {}): WallView =
    WallView(bitmapCache, dir)
        .addTo(this)
        .apply(callback)
