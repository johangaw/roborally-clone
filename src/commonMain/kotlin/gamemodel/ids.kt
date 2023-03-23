package gamemodel

@JvmInline
value class RobotId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = RobotId(freeId++)
    }
}

@JvmInline
value class WallId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = WallId(freeId++)
    }
}

@JvmInline
value class PlayerId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = PlayerId(freeId++)
    }
}

@JvmInline
value class CheckpointId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = CheckpointId(freeId++)
    }
}
