package ui

import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*

class LaserCannonView(bitmapCache: BitmapCache, dir: Direction, power: Int) : Container() {

    private val baseSize: Double = 128.0

    init {
        val bitmap = when(power) {
            1 -> bitmapCache.laserCannonX1
            2 -> bitmapCache.laserCannonX2
            3 -> bitmapCache.laserCannonX3
            else -> throw IllegalArgumentException("LaserCannon with power=$power is not supported")
        }
        image(bitmap) {
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
    power: Int,
    callback: LaserCannonView.() -> Unit = {},
): LaserCannonView = LaserCannonView(bitmapCache, dir, power)
    .addTo(this)
    .apply(callback)
