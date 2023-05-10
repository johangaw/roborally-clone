package gamemodel

import kotlin.math.*

fun GameModel.resolveAllConveyorBelts() = resolveConveyorBelts(course.conveyorBelts)

fun GameModel.resolveExpressConveyorBelts() =
    resolveConveyorBelts(course.conveyorBelts.filter { it.value.speed == ConveyorBeltSpeed.Express })

private fun GameModel.resolveConveyorBelts(conveyorBelts: Map<Pos, ConveyorBelt>): ConveyorBeltsResolutionResult {

    val movedRobots = robots
        .withConveyorBelt(conveyorBelts)
        .withNewPosition()
        .removeMovementsToSamePos()
        .removeMovementsThroughAnotherRobot(this, conveyorBelts)
        .removeMovementsThroughWalls(this)
        .removeMovementsBlockedByStationaryRobot(this)

    val rotatedRobots = movedRobots
        .withNewConveyorBelt(conveyorBelts)
        .withRotation()
        .removeRobotsWithoutRotation()
        .rotateRobots()

    val robotPartition = robots
        .applyUpdates(movedRobots, rotatedRobots)
        .partitionRunning(this)
        .damageDestroyedRobots(this)

    return ConveyorBeltsResolutionResult(
        copy(
            robots = robotPartition.running,
            destroyedRobots = this.destroyedRobots + robotPartition.destroyed
        ),
        movedRobots.associate { (robot, pos) -> robot.id to pos },
        rotatedRobots.associate { (robot, dir) -> robot.id to dir },
        robotPartition.destroyed.associate { it.id to it.health },
        conveyorBelts.keys
    )
}

private fun DestroyedRunningPartition.damageDestroyedRobots(gameModel: GameModel) =
    copy(
        destroyed = destroyed.map { it.copy(health = it.health - gameModel.course.destroyedDamage) }
    )

private data class DestroyedRunningPartition(
    val destroyed: List<Robot>,
    val running: List<Robot>,
)

private fun List<Robot>.partitionRunning(gameModel: GameModel) =
    partition { gameModel.course.isMissingFloor(it.pos) }
        .let { DestroyedRunningPartition(it.first, it.second) }

private fun List<Robot>.applyUpdates(
    movedRobots: List<Pair<Robot, Pos>>,
    rotatedRobots: List<Pair<Robot, Direction>>,
): List<Robot> {
    val movedRobotsMap = movedRobots.associate { (r, p) -> r.id to p }
    val rotatedRobotsMap = rotatedRobots.associate { (r, d) -> r.id to d }
    return map {
        it.copy(
            pos = movedRobotsMap.getOrDefault(it.id, it.pos),
            dir = rotatedRobotsMap.getOrDefault(it.id, it.dir),
        )
    }
}

private fun List<Robot>.withConveyorBelt(conveyorBelts: Map<Pos, ConveyorBelt>) =
    mapNotNull { robot -> conveyorBelts[robot.pos]?.let { belt -> robot to belt } }

private fun List<Pair<Robot, ConveyorBelt>>.withNewPosition() =
    map { (robot, belt) -> robot to robot.pos + belt.type.transportDirection }

private fun List<Pair<Robot, Pos>>.removeMovementsToSamePos() = let { list ->
    val numberOfRobotsMovedToPos = list
        .groupingBy { it.second }
        .eachCount()
    list.filter { (_, newPos) -> numberOfRobotsMovedToPos.getValue(newPos) == 1 }
}

private fun List<Pair<Robot, Pos>>.removeMovementsThroughAnotherRobot(
    gameModel: GameModel,
    conveyorBelts: Map<Pos, ConveyorBelt>,
) = let { list ->
    val movingThroughAnotherRobot: (Pair<Robot, Pos>) -> Boolean = { (robot, newPos) ->
        val robotsBelt = conveyorBelts[robot.pos]
        val otherRobotsBelt = conveyorBelts[newPos]
        gameModel.robotAt(newPos) != null && robotsBelt !== null && otherRobotsBelt != null && robotsBelt.type.transportDirection == otherRobotsBelt.type.transportDirection.opposite()
    }
    list.filter { !movingThroughAnotherRobot(it) }
}

private fun List<Pair<Robot, Pos>>.removeMovementsThroughWalls(gameModel: GameModel) = filter { (robot, newPos) ->
    gameModel.wallAt(robot.pos, direction(robot.pos, newPos)) == null
}

private fun List<Pair<Robot, Pos>>.removeMovementsBlockedByStationaryRobot(gameModel: GameModel) = let { list ->
    var newList = list
    do {
        val movedRobotIds = newList.robotIds()
        newList = newList.filter { (_, newPos) ->
            gameModel
                .robotAt(newPos)
                .let { it == null || it.id in movedRobotIds }
        }
    } while (newList.robotIds().size < movedRobotIds.size)
    newList
}

private fun List<Pair<Robot, Pos>>.robotIds() = map { it.first.id }.toSet()


private data class RobotWithNewConveyorBelt(val robot: Robot, val newPos: Pos, val newConveyorBelt: ConveyorBelt)

private fun List<Pair<Robot, Pos>>.withNewConveyorBelt(conveyorBelts: Map<Pos, ConveyorBelt>) =
    mapNotNull { (robot, newPos) -> conveyorBelts[newPos]?.let { RobotWithNewConveyorBelt(robot, newPos, it) } }

private fun List<RobotWithNewConveyorBelt>.withRotation() =
    map {
        it.robot to getRotation(getDirection(it.robot.pos, it.newPos), it.newConveyorBelt.type.transportDirection)
    }

private fun getDirection(p1: Pos, p2: Pos) =
    Direction.values().first { it.dx == (p2.x - p1.x) && it.dy == (p2.y - p1.y) }

private fun getRotation(d1: Direction, d2: Direction): Rotation {
    val dx = d1.dx + d2.dx
    val dy = d1.dy + d2.dy

    return when {
        dx == 0 && dy == 0 -> Rotation.None
        dx == dy -> Rotation.Clockwise
        abs(dx) == abs(dy) -> Rotation.CounterClockwise
        else -> Rotation.None
    }
}

private fun List<Pair<Robot, Rotation>>.removeRobotsWithoutRotation(): List<Pair<Robot, Rotation>> = filter {
    it.second != Rotation.None
}

private fun List<Pair<Robot, Rotation>>.rotateRobots() =
    map { (robot, rotation) -> robot to robot.dir.rotate(rotation) }

private fun direction(from: Pos, to: Pos): Direction = Direction
    .values()
    .first { it.dx == to.x - from.x && it.dy == to.y - from.y }


data class ConveyorBeltsResolutionResult(
    val gameModel: GameModel,
    val movedRobots: Map<RobotId, Pos>,
    val rotatedRobots: Map<RobotId, Direction>,
    val remainingHealthOfFallenRobots: Map<RobotId, Int>,
    val activatedPositions: Set<Pos>,
)
