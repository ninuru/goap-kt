package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.agent.Agent
import io.github.ninuru.goap.algorithms.AStarPlanner
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.Engine

import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario
import io.github.ninuru.goap.logs.Logger
import io.github.ninuru.goap.logs.StdoutLogger
import io.github.ninuru.goap.planner.Planner
import io.github.ninuru.goap.reports.Reporter
import io.github.ninuru.goap.state.StateProvider
import io.github.ninuru.goap.state.WorldState

/**
 * Top-level DSL receiver for configuring a GOAP AI engine.
 *
 * Supports:
 * - Overriding default planner, logger, reporter, world state, and agent
 * - Inline action definitions: `action("name") { ... }`
 * - Inline goal definitions: `goal("name") { ... }`
 * - Class-based action/goal registration
 * - World state initialization
 * - State provider registration
 * - Scenario setting
 */
@GoapDsl
class GoapSetup(private val existingAi: Goap? = null) {
    var planner: Planner = AStarPlanner()
    var logger: Logger = StdoutLogger()
    var reporter: Reporter = Reporter()
    var worldState: WorldState = WorldState()
    var agent: Agent = Agent()

    private val actions = mutableListOf<Action>()
    private val goals = mutableListOf<Goal>()
    private val stateProviders = mutableListOf<StateProvider>()
    private var scenario: Scenario? = null

    /**
     * Defines an action inline.
     */
    fun action(name: String, configure: ActionScope.() -> Unit) {
        val scope = ActionScope(name)
        scope.configure()
        actions.add(scope.build())
    }

    /**
     * Registers a pre-built action.
     */
    fun action(action: Action) {
        actions.add(action)
    }

    /**
     * Registers multiple pre-built actions.
     */
    fun actions(vararg actions: Action) {
        this.actions.addAll(actions)
    }

    /**
     * Defines a goal inline.
     */
    fun goal(name: String, configure: GoalScope.() -> Unit) {
        val scope = GoalScope(name)
        scope.configure()
        goals.add(scope.build())
    }

    /**
     * Registers a pre-built goal.
     */
    fun goal(goal: Goal) {
        goals.add(goal)
    }

    /**
     * Registers multiple pre-built goals.
     */
    fun goals(vararg goals: Goal) {
        this.goals.addAll(goals)
    }

    /**
     * Initializes world state with belief values.
     */
    fun worldState(configure: WorldStateScope.() -> Unit) {
        val scope = WorldStateScope(worldState)
        scope.configure()
    }

    /**
     * Registers a state provider from a lambda.
     */
    fun stateProvider(update: (Goap, BlackBoard, WorldState) -> Unit) {
        val provider = object : StateProvider {
            override fun updateState(ai: Goap, bb: BlackBoard, ws: WorldState) {
                update(ai, bb, ws)
            }
        }
        stateProviders.add(provider)
    }

    /**
     * Registers a pre-built state provider.
     */
    fun stateProvider(provider: StateProvider) {
        stateProviders.add(provider)
    }

    /**
     * Sets the active scenario.
     */
    fun scenario(scenario: Scenario) {
        this.scenario = scenario
    }

    internal fun build(): Goap {
        val ai = if (existingAi != null) {
            existingAi
        } else {
            Engine(worldState, planner, agent, logger, reporter)
        }

        for (action in actions) {
            ai.addAction(action)
        }

        for (goal in goals) {
            ai.addGoal(goal)
        }

        for (provider in stateProviders) {
            ai.addStateProvider(provider)
        }

        if (scenario != null) {
            ai.setScenario(scenario!!)
        }

        return ai
    }
}
