package gamemodel

import kotlin.math.*

fun setupGame(gameModel: GameModel): SetupGameResult {
    return SetupGameResult.Success(gameModel.resolveDealActionCards().gameModel)
}

fun setupGame(course: Course, playerCount: Int): SetupGameResult {
    if (playerCount > course.starts.size) return SetupGameResult.NotEnoughStartingPositions(
        course.starts.size,
        playerCount
    )

    val playerRange = 0 until playerCount
    val robots = playerRange.map {
        val pos = course.starts.sorted()[it].pos
        val dir = directionTowardsCourseCenter(course, pos)
        Robot(pos, dir)
    }
    val players = playerRange.map { Player(robots[it].id) }
    return setupGame(
        GameModel(
            course = course,
            robots = robots,
            players = players,
        )
    )
}

suspend fun setupGame(preBuildCourse: PreBuildCourse, playerCount: Int): SetupGameResult =
    setupGame(loadCourse(preBuildCourse), playerCount)

private fun directionTowardsCourseCenter(course: Course, from: Pos): Direction {
    val courseCenter = Pos(course.width / 2, course.height / 2)
    val dx = courseCenter.x - from.x
    val dy = courseCenter.y - from.y

    return when {
        abs(dx) > abs(dy) -> Direction
            .values()
            .first { it.dx == dx.normalize() && it.dy == 0 }

        abs(dx) < abs(dy) -> Direction
            .values()
            .first { it.dx == 0 && it.dy == dy.normalize() }

        else -> Direction
            .values()
            .first { it.dx == dx.normalize() && it.dy == 0 }
    }
}

sealed interface SetupGameResult {
    data class Success(val gameModel: GameModel) : SetupGameResult

    data class NotEnoughStartingPositions(val available: Int, val required: Int) : SetupGameResult
}

private fun Int.normalize() = if (this == 0) 0 else this / abs(this)
