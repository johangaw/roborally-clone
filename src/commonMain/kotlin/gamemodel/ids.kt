package gamemodel

import kotlinx.serialization.Serializable

@JvmInline
value class RobotId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = RobotId(freeId++)
    }
}

@JvmInline
@Serializable
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
@Serializable
value class CheckpointId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = CheckpointId(freeId++)
    }
}
