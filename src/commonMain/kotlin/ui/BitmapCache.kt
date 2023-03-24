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
            powerUp = resourcesVfs["power_up.png"].readBitmap(),
            again = resourcesVfs["again.png"].readBitmap(),
        )
    }
}
