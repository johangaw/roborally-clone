package ui

import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*
import gamemodel.ConveyorBeltType.*

class ConveyorBeltView(type: ConveyorBeltType, bitmapCache: BitmapCache): Container() {

    private val baseSize: Double = 128.0
    init {
        val bitmap = when(type) {
            Up -> bitmapCache.conveyorBelt
            Right -> bitmapCache.conveyorBelt
            Left -> bitmapCache.conveyorBelt
            Down -> bitmapCache.conveyorBelt
            RightAndDown -> bitmapCache.conveyorBeltTurnClockwise
            DownAndLeft -> bitmapCache.conveyorBeltTurnClockwise
            LeftAndUp -> bitmapCache.conveyorBeltTurnClockwise
            UpAndRight -> bitmapCache.conveyorBeltTurnClockwise
            RightAndUp -> bitmapCache.conveyorBeltTurnCounterClockwise
            UpAndLeft -> bitmapCache.conveyorBeltTurnCounterClockwise
            LeftAndDown -> bitmapCache.conveyorBeltTurnCounterClockwise
            DownAndRight -> bitmapCache.conveyorBeltTurnCounterClockwise
        }

        image(bitmap) {
            size(baseSize, baseSize)
            val angle = when(type) {
                Up -> -Angle.QUARTER
                Right -> Angle.ZERO
                Left -> Angle.HALF
                Down -> Angle.QUARTER

                RightAndDown -> Angle.QUARTER
                DownAndLeft-> Angle.HALF
                LeftAndUp-> -Angle.QUARTER
                UpAndRight-> Angle.ZERO

                RightAndUp -> Angle.QUARTER
                UpAndLeft -> Angle.ZERO
                LeftAndDown -> -Angle.QUARTER
                DownAndRight -> Angle.HALF
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

fun Container.conveyorBeltView(type: ConveyorBeltType, bitmapCache: BitmapCache, callback: ConveyorBeltView.() -> Unit) =
    ConveyorBeltView(type, bitmapCache).addTo(this).apply(callback)
