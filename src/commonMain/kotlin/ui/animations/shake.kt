package ui.animations

import com.soywiz.klock.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korma.interpolation.*
import kotlin.math.*

class ShakeEasing(private val time: TimeSpan): Easing {
    override fun invoke(it: Double): Double {
        if(it == 0.0 || it == 1.0) return 0.0
        return sin(it * 30 * time.millisecondsInt / 500)
    }
}

fun Animator.shake(view: View, time: TimeSpan) = tween(view::y[view.y + 2.0], easing = ShakeEasing(time), time = time)
