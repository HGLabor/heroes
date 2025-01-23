package gg.norisk.heroes.common.networking.dto

import kotlinx.serialization.Serializable

enum class MouseType {
    LEFT, MIDDLE, RIGHT
}

enum class MouseAction {
    CLICK, RELEASE, HOLD
}

@Serializable
data class MousePacket(val type: MouseType, val action: MouseAction) {
    fun isLeft(): Boolean = type == MouseType.LEFT
    fun isRight(): Boolean = type == MouseType.RIGHT
    fun isMiddle(): Boolean = type == MouseType.MIDDLE

    fun isHolding(): Boolean = action == MouseAction.HOLD
    fun isReleased(): Boolean = action == MouseAction.RELEASE
    fun isClicked(): Boolean = action == MouseAction.CLICK

    fun isHoldingLeftClick(): Boolean = isLeft() && isHolding()
    fun isHoldingRightClick(): Boolean = isRight() && isHolding()
    fun isHoldingMiddleClick(): Boolean = isMiddle() && isHolding()

    override fun toString(): String {
        return "[$type, $action]"
    }
}
