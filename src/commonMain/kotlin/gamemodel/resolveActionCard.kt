package gamemodel

import kotlin.math.*

fun GameModel.resolveActionCard(id: RobotId, card: ActionCard): ActionCardResolutionResult {
    return when (card) {
        is ActionCard.MoveForward -> resolveActionCard(id, card)
        is ActionCard.Turn -> resolveActionCard(id, card)
    }
}

private fun GameModel.resolveActionCard(id: RobotId, card: ActionCard.MoveForward): ActionCardResolutionResult {
    val robot = getRobot(id)
    val pushDirection = if (card.distance > 0) robot.dir else robot.dir.opposite()

    return (1..abs(card.distance))
        .runningFold(MovementResolution(this, emptyList(), emptyList())) { acc, _ ->
            acc.gameModel.resolveMovement(id, pushDirection)
        }
        .drop(1)
        .filter { resolution -> resolution.moves.isNotEmpty() || resolution.falls.isNotEmpty() }
        .let { resolutions ->
            ActionCardResolutionResult.MovementResult(
                gameModel = resolutions.lastOrNull()?.gameModel ?: this,
                steps = resolutions.map { MovementStep(it.moves, it.falls) })
        }
}

private fun GameModel.resolveMovement(robotId: RobotId, dir: Direction): MovementResolution {
    if (isDestroyed(robotId)) return MovementResolution(this, emptyList(), emptyList())

    val robot = getRobot(robotId)
    val maxMovement = max(course.width, course.height) // enough to scan all across the course
    val path = getPath(robot.pos, dir, maxMovement)
        .takeWhile { p ->
            wallAt(p, dir.opposite()) == null
        }
    val canMove = path.count { robotAt(it) == null } > 0

    if (!canMove) return MovementResolution(this, emptyList(), emptyList())

    val robotsToPush = path
        .takeWhile { robotAt(it) != null }
        .mapNotNull { robotAt(it) }
    val newPositions = (listOf(robot) + robotsToPush)
        .associate { it.id to it.pos + dir }
    val destroyedRobotsIds = newPositions.filterValues { course.isMissingFloor(it) }.keys

    val robots = this.robots
        .filter { it.id !in destroyedRobotsIds }
        .map { it.copy(pos = newPositions.getOrDefault(it.id, it.pos)) }
    val destroyedRobots = this.robots
        .filter { it.id in destroyedRobotsIds }
        .map { it.copy(pos = newPositions.getValue(it.id), health = it.health - this.course.destroyedDamage) }

    return MovementResolution(
        gameModel = this.copy(
            robots = robots,
            destroyedRobots = this.destroyedRobots + destroyedRobots,
        ),
        moves = newPositions.map { (id, pos) -> RobotMove(id, pos) },
        falls = destroyedRobots.map { RobotFall(it.id, it.health) },
    )
}

private data class MovementResolution(val gameModel: GameModel, val moves: List<RobotMove>, val falls: List<RobotFall>)

private fun GameModel.resolveActionCard(robotId: RobotId, card: ActionCard.Turn): ActionCardResolutionResult {
    val robot = getRobot(robotId)
    val dir = robot.dir + card.turn

    return ActionCardResolutionResult.TurningResult(
        gameModel = mapRobot(robotId) { it.copy(dir = dir) }, robotId = robotId, newDirection = dir
    )
}

private operator fun Direction.plus(rotation: Turn): Direction = when (this) {
    Direction.Up -> when (rotation) {
        Turn.Right -> Direction.Right
        Turn.Left -> Direction.Left
        Turn.UTurn -> Direction.Down
    }

    Direction.Down -> when (rotation) {
        Turn.Right -> Direction.Left
        Turn.Left -> Direction.Right
        Turn.UTurn -> Direction.Up
    }

    Direction.Right -> when (rotation) {
        Turn.Right -> Direction.Down
        Turn.Left -> Direction.Up
        Turn.UTurn -> Direction.Left
    }

    Direction.Left -> when (rotation) {
        Turn.Right -> Direction.Up
        Turn.Left -> Direction.Down
        Turn.UTurn -> Direction.Right
    }
}


private fun getPath(pos: Pos, dir: Direction, distance: Int): List<Pos> =
    (1..distance).map { Pos(pos.x + dir.dx * it, pos.y + dir.dy * it) }

sealed class ActionCardResolutionResult {
    abstract val gameModel: GameModel

    data class TurningResult(override val gameModel: GameModel, val robotId: RobotId, val newDirection: Direction) :
        ActionCardResolutionResult()

    data class MovementResult(override val gameModel: GameModel, val steps: List<MovementStep>) :
        ActionCardResolutionResult()

}

data class MovementStep(val moves: List<RobotMove>, val falls: List<RobotFall>) {
    constructor(vararg parts: MovementStepPart) : this(
        parts.toList().filterIsInstance<RobotMove>(),
        parts.toList().filterIsInstance<RobotFall>(),
    )
    constructor(vararg moves: Pair<RobotId, Pos>) : this(moves.map { (id, pos) -> RobotMove(id, pos) }, emptyList())
}

interface MovementStepPart

data class RobotMove(val robotId: RobotId, val newPos: Pos): MovementStepPart

data class RobotFall(val robotId: RobotId, val remainingHealth: Int): MovementStepPart
