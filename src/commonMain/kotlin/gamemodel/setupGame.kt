package gamemodel


fun setupGame(): GameModel {
    val playerOneRobot = Robot(Pos(4, 4), Direction.Down)
    val playerTwoRobot = Robot(Pos(4, 6), Direction.Right)

    return setupGame(
        GameModel(
            robots = listOf(
                playerOneRobot,
                playerTwoRobot,
            ),
            walls = listOf(
                Wall(Pos(2, 2), Direction.Left),
                Wall(Pos(2, 2), Direction.Right),
                Wall(Pos(2, 2), Direction.Up),
                Wall(Pos(2, 2), Direction.Down),
            ),
            players = listOf(
                Player(robotId = playerOneRobot.id),
                Player(robotId = playerTwoRobot.id),
            ),
            course = Course(
                width = 10,
                height = 10,
                checkpoints = mapOf(
                    Pos(5, 6) to Checkpoint(0, Pos(5, 6)),
                    Pos(2, 4) to Checkpoint(1, Pos(2, 4)),
                    Pos(9, 9) to Checkpoint(2, Pos(9, 9)),
                ),
                walls = emptyList(),
                conveyorBelts = emptyMap(),
            )
        )
    )
}

fun setupGame(gameModel: GameModel): GameModel {
    return gameModel.resolveDealActionCards().gameModel
}

fun setupGame(trackNumber: Int): Nothing = TODO("not implemented yet")
