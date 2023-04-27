package gamemodel

import kotlin.math.*


fun setupGame(): GameModel {
    val playerOneRobot = Robot(Pos(4, 0), Direction.Down)
    val playerTwoRobot = Robot(Pos(4, 6), Direction.Right)

    return setupGame(
        GameModel(
            robots = listOf(
                playerOneRobot,
                playerTwoRobot,
            ),
            players = listOf(
                Player(robotId = playerOneRobot.id),
                Player(robotId = playerTwoRobot.id),
            ),
            course = Course(
                width = 10,
                height = 10,
                checkpoints = listOf(
                    Checkpoint(CheckpointId(0), Pos(5, 6)),
                    Checkpoint(CheckpointId(1), Pos(2, 4)),
                    Checkpoint(CheckpointId(2), Pos(9, 9)),
                ),
                walls = listOf(
                    Wall(Pos(2, 2), Direction.Left),
                    Wall(Pos(2, 2), Direction.Right),
                    Wall(Pos(2, 2), Direction.Up),
                    Wall(Pos(2, 2), Direction.Down),
                ),
                conveyorBelts = mapOf(
                    Pos(4, 0) to ConveyorBelt(ConveyorBeltType.Up, ConveyorBeltSpeed.Regular)
                ),
                starts = listOf(
                    Start(Pos(5,5), 0)
                )
            )
        )
    )
}

fun setupGame(gameModel: GameModel): GameModel {
    return gameModel.resolveDealActionCards().gameModel
}

fun setupGame(course: Course, playerCount: Int): GameModel {
    val playerRange = 0 until playerCount
    val robots = playerRange.map {
        val pos = course.starts.sorted()[it].pos
        val dir = directionTowardsCourseCenter(course, pos)
        Robot(pos, dir)
    }
    val players = playerRange.map { Player(robots[it].id) }
    return GameModel(
        course = course,
        robots = robots,
        players = players,
    ).resolveDealActionCards().gameModel
}

suspend fun setupGame(preBuildCourse: PreBuildCourse, playerCount: Int): GameModel =
    setupGame(loadCourse(preBuildCourse), playerCount)

private fun directionTowardsCourseCenter(course: Course, from: Pos): Direction {
    val courseCenter = Pos(course.width / 2, course.height / 2)
    val dx = courseCenter.x - from.x
    val dy = courseCenter.y - from.y

    return when {
        abs(dx) > abs(dy) -> Direction.values().first { it.dx == dx.normalize() && it.dy == 0 }
        abs(dx) < abs(dy) -> Direction.values().first { it.dx == 0 && it.dy == dy.normalize() }
        else -> Direction.values().first { it.dx == dx.normalize() && it.dy == 0 }
    }
}

private fun Int.normalize() = if(this == 0) 0 else  this / abs(this)
