package io.github.ninuru.goap.action

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.action.ActionStrategy
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

abstract class ActionAbstract : Action {
    override val preConditions: Set<StateBelief> get() = emptySet()
    override val runtimeConditions: Set<StateBelief> get() = emptySet()
    override val sideEffects: Set<StateBelief> get() = emptySet()
    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}
    override fun isPossible(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return runtimeConditions.all { ws.match(it) }
    }
    override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {}
}

class ActionConstraints {
    companion object {
        fun validateAction(action: Action) {
            val preConditions = action.preConditions
            val effects = action.effects

            if (preConditions.isEmpty() && effects.isEmpty()) {
                throw IllegalArgumentException("Action ${action.name} has neither pre-conditions nor effects")
            }
        }
    }
}

abstract class ActionStrategyAbstract : ActionStrategy {
    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}
    override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {}
}

data class ActionNamedStrategy(
    val name: String,
    val strategy: ActionStrategy
)
