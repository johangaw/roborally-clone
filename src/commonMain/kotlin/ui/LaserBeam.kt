package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import gamemodel.*

class LaserBeam(cellSize: Double, length: Int, dir: Direction) :
    FixedSizeContainer(cellSize * length, cellSize) {
    init {
        val laserWidth = 5.0
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
