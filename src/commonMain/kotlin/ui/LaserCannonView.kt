package ui

import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*

class LaserCannonView(bitmapCache: BitmapCache, dir: Direction) : Container() {

    private val baseSize: Double = 128.0

    init {
        image(bitmapCache.laserCannonX1) {
            setSize(baseSize, baseSize)
            val angle = when (dir) {
                Direction.Up -> Angle.HALF
                Direction.Down -> Angle.ZERO
                Direction.Right -> -Angle.QUARTER
                Direction.Left -> Angle.QUARTER
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

fun Container.laserCannonView(
    bitmapCache: BitmapCache,
    dir: Direction,
    callback: LaserCannonView.() -> Unit = {},
): LaserCannonView = LaserCannonView(bitmapCache, dir)
    .addTo(this)
    .apply(callback)
