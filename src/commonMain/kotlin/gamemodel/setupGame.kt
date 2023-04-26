package gamemodel


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

suspend fun setupGame(preBuildCourse: PreBuildCourse, playerCount: Int): GameModel {
    val playerRange = 0 until playerCount
    val course = loadCourse(preBuildCourse)
    val robots = playerRange.map { Robot(course.starts.sorted()[it].pos, Direction.Up) }
    val players = playerRange.map { Player(robots[it].id) }
    return GameModel(
        course = course,
        robots = robots,
        players = players,
    ).resolveDealActionCards().gameModel
}
