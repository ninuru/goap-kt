package io.github.ninuru.goap.state

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard

interface StateProvider {
    fun updateState(ai: Goap, bb: BlackBoard, ws: WorldState)
}
