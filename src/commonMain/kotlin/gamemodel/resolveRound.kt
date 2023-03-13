package gamemodel

fun GameModel.resolveRound(programming: Map<PlayerId, List<ActionCard>>): ResolveRoundResult {
    if (programming.values.all { it.isEmpty() }) return ResolveRoundResult(this, emptyList())

    programming.forEach { (playerId, cards) ->
        assert(cards.isNotEmpty()) { "Player ${playerId.value} has selected fewer cards than the others" }
    }

    return programming.map { (id, cards) -> id to cards.first() }
        .sortedBy { (_, card) -> card.initiative }
        .runningFold<Pair<PlayerId, ActionCard>,Pair<GameModel, ResolutionStep?>>(this to null) { (gameModel, _), (playerId, card) ->
            gameModel.controlRobot(gameModel.getPlayer(playerId).robotId, card).let { result ->
                when(result) {
                    is RobotActionResult.Moved -> result.gameModel to ResolutionStep.MoveRobot(result.moveSteps)
                    is RobotActionResult.Turned -> result.gameModel to ResolutionStep.RotateRobot(result.robotId, result.newDirection)
                }
            }
        }
        .let { stepsWithModel -> stepsWithModel.last().first to stepsWithModel.map { (_, steps) -> steps } }
        .let { (model, steps) ->
            model.resolveRound(
                programming.mapValues { (_, cards) -> cards.drop(1) }
            ).let { nextResolutionResult ->
                nextResolutionResult.copy(
                    steps = steps.filterNotNull() + nextResolutionResult.steps
                )
            }
        }
}

data class ResolveRoundResult(val gameModel: GameModel, val steps: List<ResolutionStep>)

sealed class ResolutionStep {
    data class MoveRobot(val steps: List<Map<RobotId, Pos>>): ResolutionStep()
    data class RotateRobot(val robotId: RobotId, val newDirection: Direction): ResolutionStep()
}
