package io.github.ninuru.goap

import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.agent.Agent
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario
import io.github.ninuru.goap.logs.Logger
import io.github.ninuru.goap.planner.Planner
import io.github.ninuru.goap.reports.Reporter
import io.github.ninuru.goap.state.StateProvider
import io.github.ninuru.goap.state.WorldState

interface Goap {
    fun tick()
    val worldState: WorldState
    val planner: Planner
    val agent: Agent
    val blackBoard: BlackBoard
    val logger: Logger
    val reporter: Reporter
    fun getGoals(): List<Goal>
    fun getGoals(minRelevance: Int): List<Goal>
    val actions: List<Action>
    val availableActions: List<Action>
    val scenario: Scenario
    fun setScenario(scenario: Scenario)
    fun addGoal(goal: Goal)
    fun addAction(action: Action)
    fun addActions(vararg actions: Action)
    fun addStateProvider(stateProvider: StateProvider)
    fun reset()
    fun sleep(ticks: Int)
}
