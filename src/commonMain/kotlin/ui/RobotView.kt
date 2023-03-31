package ui

import com.soywiz.klock.*
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.*
import com.soywiz.korim.color.*
import gamemodel.*

class RobotView(playerNumber: PlayerNumber, direction: Direction, atlas: Atlas) : Sprite() {
    private val downAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-down")
    private val rightAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-right")
    private val leftAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-left")
    private val upAnimation = atlas.getSpriteAnimation(prefix = "robot${playerNumber.number}-up")

    private val dirMap = mapOf(
        Direction.Left to leftAnimation,
        Direction.Right to rightAnimation,
        Direction.Up to upAnimation,
        Direction.Down to downAnimation
    )

    init {
        playAnimation(0, dirMap.getValue(direction), endFrame = 1)
    }

    var direction: Direction = direction
        set(value) {
            field = value
            playAnimation(0, dirMap.getValue(value), endFrame = 1)
            currentSpriteIndex
        }

    var burning: Double = 0.0
        set(value) {
            field = value
            colorMul = value.interpolate(Colors.WHITE, Colors.BLACK)
        }

    fun playAnimation(time: TimeSpan) {
        onAnimationCompleted.once {
            this.setFrame(0)  // No Idea why it should be 0 here and 1 earlier
        }
        playAnimationForDuration(time)
    }
}

fun Container.robotView(
    playerNumber: PlayerNumber,
    direction: Direction,
    atlas: Atlas,
    callback: @ViewDslMarker() (RobotView.() -> Unit) = {}
) =
    RobotView(playerNumber, direction, atlas).addTo(this, callback)
