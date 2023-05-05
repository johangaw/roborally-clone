package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import gamemodel.*

private val baseSize = 102.0

class PitView(surroundingPits: SurroundingPits, bitmapCache: BitmapCache) : Container() {

    init {
        roundRect(baseSize, baseSize, 0.0, fill = Colors.TRANSPARENT_WHITE) {
            image(bitmapCache.pitBottom) {
                size(baseSize, baseSize)
                centerOn(parent!!)
            }

            surroundingPits
                .orthogonalPitsWithDirections()
                .filter { !it.first.get() }
                .map { it.second }
                .map(::getAngle)
                .forEach {
                    image(bitmapCache.pitEdge) {
                        size(baseSize, baseSize)
                        setTransform(
                            Matrix()
                                .translate(-baseSize / 2.0, -baseSize / 2.0)
                                .rotate(it)
                                .translate(baseSize / 2.0, baseSize / 2.0)
                                .toTransform()
                        )
                    }
                }

            DiagonalDirection
                .values()
                .filter {
                    surroundingPits
                        .getPitsInCorner(it)
                        .let { pitsInCorner -> !pitsInCorner.corner && pitsInCorner.neighbor1 && pitsInCorner.neighbor2 }
                }
                .map(::getAngle)
                .forEach {
                    image(bitmapCache.pitCorner) {
                        size(baseSize, baseSize)
                        setTransform(
                            Matrix()
                                .translate(-baseSize / 2.0, -baseSize / 2.0)
                                .rotate(it)
                                .translate(baseSize / 2.0, baseSize / 2.0)
                                .toTransform()
                        )
                    }
                }
        }
    }

    private fun getAngle(edgeDirection: Direction): Angle = when (edgeDirection) {
        Direction.Right -> Angle.ZERO
        Direction.Down -> Angle.QUARTER
        Direction.Left -> Angle.HALF
        Direction.Up -> -Angle.QUARTER
    }

    private fun getAngle(cornerDirection: DiagonalDirection): Angle = when (cornerDirection) {
        DiagonalDirection.UpRight -> Angle.ZERO
        DiagonalDirection.DownRight -> Angle.QUARTER
        DiagonalDirection.DownLeft -> Angle.HALF
        DiagonalDirection.UpLeft -> -Angle.QUARTER
    }
}

fun Container.pitView(surroundingPits: SurroundingPits, bitmapCache: BitmapCache, callback: PitView.() -> Unit = {}) =
    PitView(surroundingPits, bitmapCache)
        .addTo(this)
        .apply(callback)

data class SurroundingPits(
    val upLeft: Boolean,
    val up: Boolean,
    val upRight: Boolean,
    val left: Boolean,
    val right: Boolean,
    val downLeft: Boolean,
    val down: Boolean,
    val downRight: Boolean,
) {
    companion object {
        val ALONE_PIT
            get() = SurroundingPits(
                upLeft = false,
                up = false,
                upRight = false,
                left = false,
                right = false,
                downLeft = false,
                down = false,
                downRight = false
            )
    }
}

private fun SurroundingPits.orthogonalPitsWithDirections() = listOf(
    this::up to Direction.Up,
    this::right to Direction.Right,
    this::down to Direction.Down,
    this::left to Direction.Left,
)

private enum class DiagonalDirection {
    UpLeft, UpRight, DownLeft, DownRight
}

private data class PitsInCorner(val corner: Boolean, val neighbor1: Boolean, val neighbor2: Boolean)

private fun SurroundingPits.getPitsInCorner(direction: DiagonalDirection) = when (direction) {
    DiagonalDirection.UpLeft -> PitsInCorner(this.upLeft, this.up, this.left)
    DiagonalDirection.UpRight -> PitsInCorner(this.upRight, this.up, this.right)
    DiagonalDirection.DownLeft -> PitsInCorner(this.downLeft, this.down, this.left)
    DiagonalDirection.DownRight -> PitsInCorner(this.downRight, this.down, this.right)
}


