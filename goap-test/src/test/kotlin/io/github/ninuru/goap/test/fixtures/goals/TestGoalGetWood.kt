package io.github.ninuru.goap.test.fixtures.goals

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.GoalAbstract
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class TestGoalGetWood : GoalAbstract() {
    override val name: String = "get_wood"

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("has_axe")
    }

    override val conditions: Set<StateBelief> = setOf(StateBelief.of("has_axe"))
}
