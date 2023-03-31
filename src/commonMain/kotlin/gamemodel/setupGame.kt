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
            checkpoints = listOf(
                Checkpoint(0, Pos(5, 6)),
                Checkpoint(1, Pos(2, 4)),
                Checkpoint(2, Pos(9, 9)),
            )
        )
    )
}

fun setupGame(gameModel: GameModel): GameModel {
    return gameModel.resolveDealActionCards().gameModel
}

fun setupGame(trackNumber: Int): Nothing = TODO("not implemented yet")
