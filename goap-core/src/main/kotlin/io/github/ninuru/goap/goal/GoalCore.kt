package io.github.ninuru.goap.goal

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

abstract class GoalAbstract : Goal {
    override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
    override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
}

abstract class ScenarioAbstract : Scenario {
    override val goals: List<Goal> = emptyList()
}
