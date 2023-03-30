package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.*
import com.soywiz.korim.color.*
import gamemodel.*

class RobotView(val robotId: RobotId, playerNumber: PlayerNumber, direction: Direction, atlas: Atlas, cellSize: Double) : Container() {
    private val downAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-down")
    private val rightAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-right")
    private val leftAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-left")
    private val upAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-up")
    private val idleFrame = 1

    private val dirMap = mapOf(
        Direction.Left to leftAnimation,
        Direction.Right to rightAnimation,
        Direction.Up to upAnimation,
        Direction.Down to downAnimation
    )

    private val spriteView: Sprite = sprite(downAnimation) {
        size(cellSize, cellSize)
        setFrame(idleFrame)
    }

    var direction: Direction = direction
        set(value) {
            field = value
            spriteView.playAnimation(dirMap.getValue(value))
            spriteView.setFrame(idleFrame)
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
    playerNumber: PlayerNumber,
    direction: Direction,
    atlas: Atlas,
    cellSize: Double,
    callback: @ViewDslMarker() (RobotView.() -> Unit) = {}
) =
    RobotView(robotId, playerNumber, direction, atlas, cellSize).addTo(this, callback)
