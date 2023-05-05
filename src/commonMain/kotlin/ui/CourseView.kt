package ui

import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import gamemodel.*
import kotlin.math.*

class CourseView(val course: Course, bitmapCache: BitmapCache, showStartPositions: Boolean) : Container() {
    private val COURSE_SIZE = 2048.0
    val cellSize: Double get() = scaledWidth / course.width

    private var onClickHandler: (pos: Pos) -> Unit = {}
    var conveyorBelts: Map<Pos, ConveyorBeltView> = emptyMap()
        private set

    init {
        val cellSize = min(COURSE_SIZE / course.width, COURSE_SIZE / course.height)
        repeat(course.width) { x ->
            repeat(course.height) { y ->
                image(bitmapCache.floor) {
                    size(cellSize, cellSize)
                    position(x * cellSize, y * cellSize)
                    onClick { onClickHandler(Pos(x, y)) }
                }
            }
        }

        course.pits.forEach { pitPos ->
            pitView(
                SurroundingPits(
                    upLeft = pitPos + Direction.Up + Direction.Left in course.pits,
                    up = pitPos + Direction.Up in course.pits,
                    upRight = pitPos + Direction.Up + Direction.Right in course.pits,
                    left = pitPos + Direction.Left in course.pits,
                    right = pitPos + Direction.Right in course.pits,
                    downLeft = pitPos + Direction.Down + Direction.Left in course.pits,
                    down = pitPos + Direction.Down in course.pits,
                    downRight = pitPos + Direction.Down + Direction.Right in course.pits,
                ), bitmapCache
            ) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(pitPos))
            }
        }

        conveyorBelts = course.conveyorBelts.mapValues { (pos, belt) ->
            conveyorBeltView(belt.type, belt.speed, bitmapCache) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(pos))
            }
        }

        course.walls.forEach {
            wallView(bitmapCache, it.dir) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(it.pos))
            }
        }

        course.laserCannons.forEach {
            laserCannonView(bitmapCache, it.dir, it.power) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(it.pos))
            }
        }

        if (showStartPositions)
            course.starts.forEach {
                startView(it.order) {
                    setSizeScaled(cellSize, cellSize)
                    position(getPoint(it.pos))
                }
            }

        course.checkpoints.forEach {
            checkpointView(it.id, bitmapCache) {
                setSizeScaled(cellSize, cellSize)
                position(getPoint(it.pos))
            }
        }
    }

    private fun getPoint(pos: Pos): IPoint = Point(pos.x * cellSize, pos.y * cellSize)

    fun onClick(handler: (pos: Pos) -> Unit) {
        onClickHandler = handler
    }
}

fun Container.courseView(
    course: Course,
    bitmapCache: BitmapCache,
    showStartPositions: Boolean = true,
    callback: CourseView.() -> Unit = {},
) =
    CourseView(course, bitmapCache, showStartPositions)
        .addTo(this)
        .apply(callback)
