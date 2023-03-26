package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import gamemodel.*

class LaserBeam(cellSize: Double, length: Int, dir: LaserDirection) :
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
            LaserDirection.Right -> Angle.ZERO
            LaserDirection.Left -> Angle.HALF
            LaserDirection.Down -> Angle.QUARTER
            LaserDirection.Up -> Angle.QUARTER + Angle.HALF
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
