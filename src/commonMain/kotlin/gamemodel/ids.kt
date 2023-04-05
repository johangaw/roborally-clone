package gamemodel

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class RobotId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = RobotId(freeId++)
    }
}

@JvmInline
@Serializable
value class PlayerId(val value: Int) {
    companion object {
        private var freeId = 0
        fun create() = PlayerId(freeId++)
    }
}
