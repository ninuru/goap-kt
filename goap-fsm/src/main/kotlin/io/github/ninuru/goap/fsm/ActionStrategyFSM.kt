package io.github.ninuru.goap.fsm

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.ActionStrategy
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.WorldState

class ActionStrategyFSM(private val fsm: FSM) : ActionStrategy {

    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {
        fsm.activate()
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        return fsm.tick(ai, bb, ws)
    }

    override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {
        fsm.deactivate()
    }

    override fun allowInventory(): Boolean {
        return fsm.allowInventory()
    }
}
