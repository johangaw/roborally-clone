package ui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.*


private val baseSize = 128.0

class StartView(order: Int) : Container() {

    init {
        roundRect(baseSize, baseSize, 0.0, fill = Colors.TRANSPARENT_WHITE) {
            circle(baseSize/2, fill = Colors.TRANSPARENT_WHITE, stroke = Colors.BLACK, strokeThickness = 12.0) {
                scale = 0.6
                centerOn(parent!!)
                text(order.toString()) {
                    textSize = 100.0
                    color = Colors.BLACK
                    centerOn(parent!!)
                }
            }
        }
    }
}

fun Container.startView(order: Int, callback: StartView.() -> Unit = {}) =
    StartView(order).addTo(this).apply(callback)
