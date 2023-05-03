import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.*

class MyScene : Scene() {
    private lateinit var img2: RoundRect

    override suspend fun SContainer.sceneMain() {

        val img1 = solidRect(120.0, 120.0, Colors.RED)
        img2 = roundRect(120.0, 120.0, 0.0) {
            zIndex = 1.0
            alignLeftToRightOf(img1)

            image(resourcesVfs["korge.png"].readBitmap()) {
                scaledWidth = 120.0
                scaledHeight = 120.0

            }

            val dx = localMatrix.tx + scaledWidth / 2
            val dy = localMatrix.ty + scaledHeight / 2
            setTransform(
                localMatrix
                    .copy()
                    .translate(-dx, -dy)
                    .rotate(Angle.QUARTER)
                    .translate(dx, dy)
                    .toTransform()
            )
        }
        solidRect(120.0, 120.0, Colors.RED) {
            alignLeftToRightOf(img2)
        }

        keys {
            down {
                when (it.key) {
                    Key.R -> {
                        img2.apply {
                            val dx = this.globalBounds.x + scaledWidth / 2
                            val dy = this.globalBounds.y + scaledHeight / 2

                            setTransform(
                                localMatrix
                                    .copy()
                                    .translate(-dx, -dy)
                                    .rotate(Angle.QUARTER)
                                    .translate(dx, dy)
                                    .toTransform()
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
