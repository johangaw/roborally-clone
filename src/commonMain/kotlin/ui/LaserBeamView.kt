package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import gamemodel.*

class LaserBeamView(cellSize: Double, length: Int, dir: Direction, power: Int) :
    FixedSizeContainer(cellSize * length, cellSize) {

    private val multipleLaserOffset = 12.0
    init {
        val b1 = LaserBeam(cellSize, length, dir)
        val b2 = LaserBeam(cellSize, length, dir)
        val b3 = LaserBeam(cellSize, length, dir)

        when(power) {
            1 -> b1.addTo(this)
            2 -> {
                b1.addTo(this)
                b2.addTo(this)
                offsetBeams(dir, b1, b2)
            }
            3 -> {
                b1.addTo(this)
                b2.addTo(this)
                b3.addTo(this)
                offsetBeams(dir, b2, b3)
            }
        }
    }

    private fun offsetBeams(dir: Direction, b1: LaserBeam, b2: LaserBeam) {
        when(dir.isVertical()) {
            true -> {
                b1.position(b1.x - multipleLaserOffset, b1.y)
                b2.position(b2.x + multipleLaserOffset, b2.y)
            }
            false -> {
                b1.position(b1.x, b1.y - multipleLaserOffset)
                b2.position(b2.x, b2.y + multipleLaserOffset)
            }
        }
    }
}

private fun Direction.isVertical() = when(this) {
    Direction.Up -> true
    Direction.Down -> true
    Direction.Right -> false
    Direction.Left -> false
}

private class LaserBeam(cellSize: Double, length: Int, dir: Direction) :
    FixedSizeContainer(cellSize * length, cellSize) {
    init {
        val laserWidth = 3.0
        val laserBallRadius = laserWidth * 2

        val beam = roundRect(cellSize * length, laserWidth, 3.0, 3.0, fill = Colors.RED) {
            centerOn(parent!!)
        }
        circle(laserBallRadius, fill = Colors.RED, autoScaling = false) {
            centerOn(beam)
            alignRightToRightOf(beam)
        }

        val angle = when (dir) {
            Direction.Right -> Angle.ZERO
            Direction.Left -> Angle.HALF
            Direction.Down -> Angle.QUARTER
            Direction.Up -> Angle.QUARTER + Angle.HALF
        }

        setTransform(
            Matrix()
                .translate(-cellSize / 2.0, -cellSize / 2.0)
                .rotate(angle)
                .translate(cellSize / 2.0, cellSize / 2)
                .toTransform()
        )
    }
}
