package io.github.ninuru.goap.fsm

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.WorldState

interface FSM {
    fun activate()
    fun tick(ai: Goap, bb: BlackBoard, ws: WorldState): Status
    fun deactivate()
    fun allowInventory(): Boolean
}
