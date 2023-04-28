package ui

import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

data class BitmapCache(
    val backUp: Bitmap,
    val checkpoint: Bitmap,
    val checkpointTaken: Bitmap,
    val forward1: Bitmap,
    val forward2: Bitmap,
    val forward3: Bitmap,
    val left: Bitmap,
    val right: Bitmap,
    val uTurn: Bitmap,
    val floor: Bitmap,
    val conveyorBelt: Bitmap,
    val conveyorBeltTurnClockwise: Bitmap,
    val conveyorBeltTurnCounterClockwise: Bitmap,
    val wall: Bitmap,
    val heart: Bitmap,
    val laserCannonX1: Bitmap,
    val laserCannonX2: Bitmap,
    val laserCannonX3: Bitmap,

    val powerUp: Bitmap,
    val again: Bitmap,
) {

    companion object {
        suspend fun create() = BitmapCache(
            backUp = resourcesVfs["back_up.png"].readBitmap(),
            checkpoint = resourcesVfs["checkpoint.png"].readBitmap(),
            checkpointTaken = resourcesVfs["checkpoint_taken.png"].readBitmap(),
            forward1 = resourcesVfs["forward1.png"].readBitmap(),
            forward2 = resourcesVfs["forward2.png"].readBitmap(),
            forward3 = resourcesVfs["forward3.png"].readBitmap(),
            left = resourcesVfs["left.png"].readBitmap(),
            right = resourcesVfs["right.png"].readBitmap(),
            uTurn = resourcesVfs["u_turn.png"].readBitmap(),
            floor = resourcesVfs["floor.png"].readBitmap(),
            conveyorBelt = resourcesVfs["conveyor_belt.png"].readBitmap(),
            conveyorBeltTurnClockwise = resourcesVfs["conveyor_belt_turn_clockwise.png"].readBitmap(),
            conveyorBeltTurnCounterClockwise = resourcesVfs["conveyor_belt_turn_counter_clockwise.png"].readBitmap(),
            wall = resourcesVfs["wall.png"].readBitmap(),
            heart = resourcesVfs["heart.png"].readBitmap(),
            laserCannonX1 = resourcesVfs["laser_cannon_x1.png"].readBitmap(),
            laserCannonX2 = resourcesVfs["laser_cannon_x2.png"].readBitmap(),
            laserCannonX3 = resourcesVfs["laser_cannon_x3.png"].readBitmap(),
            powerUp = resourcesVfs["power_up.png"].readBitmap(),
            again = resourcesVfs["again.png"].readBitmap(),
        )
    }
}
