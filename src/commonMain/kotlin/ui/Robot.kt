package ui

import com.soywiz.klock.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.mask.*
import com.soywiz.korim.atlas.*
import com.soywiz.korim.color.*
import gamemodel.*

class RobotView(val robotId: RobotId, direction: Direction, atlas: Atlas, cellSize: Double) : Container() {

    private val downAnimation = atlas.getSpriteAnimation(prefix = "red-down")
    private val rightAnimation = atlas.getSpriteAnimation(prefix = "red-right")
    private val leftAnimation = atlas.getSpriteAnimation(prefix = "red-left")
    private val upAnimation = atlas.getSpriteAnimation(prefix = "red-up")

    private val dirMap = mapOf(
        Direction.Left to leftAnimation,
        Direction.Right to rightAnimation,
        Direction.Up to upAnimation,
        Direction.Down to downAnimation
    )

    private val spriteView: Sprite = sprite(downAnimation) {
        size(cellSize, cellSize)
        playAnimation(dirMap.getValue(direction))
    }

    var direction: Direction = direction
        set(value) {
            field = value
            spriteView.playAnimation(dirMap.getValue(value))
            spriteView.currentSpriteIndex
        }

    var burning: Double = 0.0
        set(value) {
            field = value
            val color = value.interpolate(Colors.WHITE, Colors.BLACK)
            spriteView.colorMul = color
        }
}

fun Container.robotView(
    robotId: RobotId,
    direction: Direction,
    atlas: Atlas,
    cellSize: Double,
    callback: @ViewDslMarker() (RobotView.() -> Unit) = {}
) =
    RobotView(robotId, direction, atlas, cellSize).addTo(this, callback)
