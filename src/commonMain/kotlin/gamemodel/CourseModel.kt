package gamemodel

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val width: Int,
    val height: Int,
    val conveyorBelts: Map<Pos, ConveyorBelt> = emptyMap(),
    val walls: List<Wall> = emptyList(),
    val checkpoints: List<Checkpoint> = emptyList(),
    val starts: List<Start> = emptyList(),
    val laserCannons: List<LaserCannon> = emptyList(),
    val pits: Set<Pos> = emptySet(),
    val gears: Map<Pos, Gear> = emptyMap(),
    val destroyedDamage: Int = 2
) {
    fun wallAt(pos: Pos, dir: Direction): Wall? {
        return walls.firstOrNull { it.pos == pos && it.dir == dir }
            ?: walls.firstOrNull { it.pos == pos + dir && it.dir == dir.opposite() }
    }

    fun getCheckpoint(id: CheckpointId) =
        checkpoints.firstOrNull { it.id == id } ?: throw AssertionError("No checkpoint with id $id")

    fun isMissingFloor(pos: Pos): Boolean = !isOnCourse(pos) || pos in pits

    fun isOnCourse(pos: Pos): Boolean = pos.x in 0 until width && pos.y in 0 until height

    init {
        assert(checkpoints.distinctBy { it.id }.size == checkpoints.size) { "Two checkpoints share the same id" }
        assert(starts.distinctBy { it.order }.size == starts.size) { "Two starts share the same order" }
    }
}

@Serializable
data class Start(val pos: Pos, val order: Int): Comparable<Start> {
    override fun compareTo(other: Start): Int = order - other.order
}

@Serializable
data class LaserCannon(val pos: Pos, val dir: Direction, val power: Int)

@Serializable
data class Checkpoint(val id: CheckpointId, val pos: Pos): Comparable<Checkpoint> {
    override fun compareTo(other: Checkpoint): Int = id.compareTo(other.id)
}

@JvmInline
@Serializable
value class CheckpointId(val value: Int) : Comparable<CheckpointId> {
    override fun compareTo(other: CheckpointId): Int = value - other.value
}

@Serializable
data class Wall(val pos: Pos, val dir: Direction)

@Serializable
data class ConveyorBelt(val type: ConveyorBeltType, val speed: ConveyorBeltSpeed)


@Serializable
enum class ConveyorBeltSpeed {
    Regular,
    Express
}

@Serializable
enum class ConveyorBeltType(val transportDirection: Direction) {
    Up(Direction.Up),
    Right(Direction.Right),
    Left(Direction.Left),
    Down(Direction.Down),

    // Clockwise
    RightAndDown(Direction.Down),
    DownAndLeft(Direction.Left),
    LeftAndUp(Direction.Up),
    UpAndRight(Direction.Right),

    // Counterclockwise
    RightAndUp(Direction.Up),
    UpAndLeft(Direction.Left),
    LeftAndDown(Direction.Down),
    DownAndRight(Direction.Right),

    // Y conjunctions
    UpAndRightToDown(Direction.Down),
    RightAndDownToLeft(Direction.Left),
    DownAndLeftToUp(Direction.Up),
    LeftAndUpToRight(Direction.Right),

    // T conjunctions
    LeftAndRightToUp(Direction.Up),
    LeftAndRightToDown(Direction.Down),
    UpAndDownToLeft(Direction.Left),
    UpAndDownToRight(Direction.Right),
}

@Serializable
data class Gear(val rotation: Rotation)
