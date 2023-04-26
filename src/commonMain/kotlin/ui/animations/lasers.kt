package ui.animations

import com.soywiz.klock.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.interpolation.*
import kotlin.math.*

class LaserEasing: Easing {
    override fun invoke(it: Double): Double {
        return 0.5*(1-cos(when(it) {
            in 0.0..0.4 -> it * Math.PI / 0.1
            in 0.4..1.0 -> (it - 0.4) * Math.PI / 0.3
            else -> 0.0
        }))
    }
}

fun Animator.laserBurn(view: View, time: TimeSpan) = tween(view::colorMul[Colors.WHITE, Colors.BLACK], time = time, easing = LaserEasing())

fun Animator.laserAlpha(view: View, time: TimeSpan) = tween(view::alpha[1.0, 0.0], time = time, easing = LaserEasing())
