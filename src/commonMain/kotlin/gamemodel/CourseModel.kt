package gamemodel

data class Course(val width: Int, val height: Int, val conveyorBelts: List<ConveyorBelt>)

data class ConveyorBelt(val type: ConveyorBeltType, val speed: ConveyorBeltSpeed, val pos: Pos)


enum class ConveyorBeltSpeed(val speed: Int) {
    Regular(1),
    Fast(2)
}

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

enum class Rotation {
    None,
    Clockwise,
    CounterClockwise
}
