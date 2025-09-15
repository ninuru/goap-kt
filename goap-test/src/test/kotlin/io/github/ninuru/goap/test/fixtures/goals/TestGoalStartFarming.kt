package io.github.ninuru.goap.test.fixtures.goals

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.GoalAbstract
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class TestGoalStartFarming : GoalAbstract() {
    override val name: String = "start_farming"

    override fun getRelevance(ai: Goap, bb: BlackBoard, ws: WorldState): Int = 2

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_garden")
    }

    override val conditions: Set<StateBelief> = setOf(StateBelief.of("in_garden"))
}
