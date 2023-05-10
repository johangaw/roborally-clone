package ui

import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*
import gamemodel.ConveyorBeltType.*

class ConveyorBeltView(type: ConveyorBeltType, speed: ConveyorBeltSpeed, bitmapCache: BitmapCache) : Container() {

    private val baseSize: Double = 128.0

    init {
        val bitmap = when (speed) {
            ConveyorBeltSpeed.Regular -> when (type) {
                Down, Left, Up, Right -> bitmapCache.conveyorBelt
                UpAndRight, LeftAndUp, RightAndDown, DownAndLeft -> bitmapCache.conveyorBeltTurnClockwise
                DownAndRight, LeftAndDown, UpAndLeft, RightAndUp -> bitmapCache.conveyorBeltTurnCounterClockwise
                UpAndRightToDown, RightAndDownToLeft, DownAndLeftToUp, LeftAndUpToRight -> bitmapCache.conveyorBeltYIntersection
                LeftAndRightToUp, LeftAndRightToDown, UpAndDownToLeft, UpAndDownToRight -> bitmapCache.conveyorBeltTIntersection
            }
            ConveyorBeltSpeed.Express -> when (type) {
                Down, Left, Up, Right -> bitmapCache.expressConveyorBelt
                UpAndRight, LeftAndUp, RightAndDown, DownAndLeft -> bitmapCache.expressConveyorBeltTurnClockwise
                DownAndRight, LeftAndDown, UpAndLeft, RightAndUp -> bitmapCache.expressConveyorBeltTurnCounterClockwise
                UpAndRightToDown, RightAndDownToLeft, DownAndLeftToUp, LeftAndUpToRight -> bitmapCache.expressConveyorBeltYIntersection
                LeftAndRightToUp, LeftAndRightToDown, UpAndDownToLeft, UpAndDownToRight -> bitmapCache.expressConveyorBeltTIntersection
            }
        }

        image(bitmap) {
            size(baseSize, baseSize)
            val angle = when (type) {
                Up -> -Angle.QUARTER
                Right -> Angle.ZERO
                Left -> Angle.HALF
                Down -> Angle.QUARTER

                RightAndDown -> Angle.QUARTER
                DownAndLeft -> Angle.HALF
                LeftAndUp -> -Angle.QUARTER
                UpAndRight -> Angle.ZERO

                RightAndUp -> Angle.QUARTER
                UpAndLeft -> Angle.ZERO
                LeftAndDown -> -Angle.QUARTER
                DownAndRight -> Angle.HALF

                UpAndRightToDown -> Angle.ZERO
                RightAndDownToLeft -> Angle.QUARTER
                DownAndLeftToUp -> Angle.HALF
                LeftAndUpToRight -> -Angle.QUARTER

                LeftAndRightToUp -> Angle.QUARTER
                LeftAndRightToDown -> -Angle.QUARTER
                UpAndDownToLeft -> Angle.ZERO
                UpAndDownToRight -> Angle.HALF
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

fun Container.conveyorBeltView(
    type: ConveyorBeltType,
    speed: ConveyorBeltSpeed,
    bitmapCache: BitmapCache,
    callback: ConveyorBeltView.() -> Unit = {},
) =
    ConveyorBeltView(type, speed, bitmapCache)
        .addTo(this)
        .apply(callback)
