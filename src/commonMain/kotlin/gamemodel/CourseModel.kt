package gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val width: Int,
    val height: Int,
    val conveyorBelts: Map<Pos, ConveyorBelt>,
    val walls: List<Wall>,
    val checkpoints: Map<Pos, Checkpoint>,
) {
    fun wallAt(pos: Pos, dir: Direction): Wall? {
        return walls.firstOrNull { it.pos == pos && it.dir == dir }
            ?: walls.firstOrNull { it.pos == pos + dir && it.dir == dir.opposite() }
    }

    fun getCheckpoint(id: CheckpointId) =
        checkpoints.values.firstOrNull { it.id == id } ?: throw AssertionError("No checkpoint with id $id")
}

@Serializable
data class Checkpoint(val order: Int, val pos: Pos, val id: CheckpointId = CheckpointId.create()) // TODO remove pos

@Serializable
data class Wall(val pos: Pos, val dir: Direction)

@Serializable
data class ConveyorBelt(val type: ConveyorBeltType, val speed: ConveyorBeltSpeed)


@Serializable
enum class ConveyorBeltSpeed(val speed: Int) {
    Regular(1),
    Fast(2)
}

@Serializable
enum class ConveyorBeltType(val transportDirection: Direction, val rotation: Rotation) {
    Up(Direction.Up, Rotation.None),
    Right(Direction.Right, Rotation.None),
    Left(Direction.Left, Rotation.None),
    Down(Direction.Down, Rotation.None),

    // Clockwise
    RightAndDown(Direction.Down, Rotation.Clockwise),
    DownAndLeft(Direction.Left, Rotation.Clockwise),
    LeftAndUp(Direction.Up, Rotation.Clockwise),
    UpAndRight(Direction.Right, Rotation.Clockwise),

    // Counterclockwise
    RightAndUp(Direction.Up, Rotation.CounterClockwise),
    UpAndLeft(Direction.Left, Rotation.CounterClockwise),
    LeftAndDown(Direction.Down, Rotation.CounterClockwise),
    DownAndRight(Direction.Right, Rotation.CounterClockwise),
}

@Serializable
enum class Rotation {
    None,
    Clockwise,
    CounterClockwise
}
