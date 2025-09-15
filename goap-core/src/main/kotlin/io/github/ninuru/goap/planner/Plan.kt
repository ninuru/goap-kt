package io.github.ninuru.goap.planner

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario
import io.github.ninuru.goap.state.StateProvider
import io.github.ninuru.goap.state.WorldState

class Plan(
    val goal: Goal,
    private val actions: List<Action> = emptyList()
) {
    private var currentIndex = 0

    fun nextAction(): Action? {
        if (currentIndex >= actions.size) return null
        return actions[currentIndex++]
    }

    fun containsAction(name: String): Boolean {
        return actions.any { it.name == name }
    }

    fun simulate(ws: WorldState): Status {
        var currentState = ws.clone()
        val dummyBlackBoard = BlackBoard()

        for (action in actions) {
            if (!action.isPossible(
                    object : Goap {
                        override fun tick() {}
                        override val worldState = currentState
                        override val planner = throw UnsupportedOperationException()
                        override val agent = throw UnsupportedOperationException()
                        override val blackBoard = dummyBlackBoard
                        override val logger = throw UnsupportedOperationException()
                        override val reporter = throw UnsupportedOperationException()
                        override fun getGoals() = emptyList<Goal>()
                        override fun getGoals(minRelevance: Int) = emptyList<Goal>()
                        override val actions = emptyList<Action>()
                        override val availableActions = emptyList<Action>()
                        override val scenario = throw UnsupportedOperationException()
                        override fun setScenario(scenario: Scenario) {}
                        override fun addGoal(goal: Goal) {}
                        override fun addAction(action: Action) {}
                        override fun addActions(vararg actions: Action) {}
                        override fun addStateProvider(stateProvider: StateProvider) {}
                        override fun reset() {}
                        override fun sleep(ticks: Int) {}
                    },
                    dummyBlackBoard,
                    currentState
                )
            ) return Status.FAILED

            for (effect in action.effects) {
                currentState.set(effect)
            }
        }
        return if (goal.isCompleted(
                object : Goap {
                    override fun tick() {}
                    override val worldState = currentState
                    override val planner = throw UnsupportedOperationException()
                    override val agent = throw UnsupportedOperationException()
                    override val blackBoard = dummyBlackBoard
                    override val logger = throw UnsupportedOperationException()
                    override val reporter = throw UnsupportedOperationException()
                    override fun getGoals() = emptyList<Goal>()
                    override fun getGoals(minRelevance: Int) = emptyList<Goal>()
                    override val actions = emptyList<Action>()
                    override val availableActions = emptyList<Action>()
                    override val scenario = throw UnsupportedOperationException()
                    override fun setScenario(scenario: Scenario) {}
                    override fun addGoal(goal: Goal) {}
                    override fun addAction(action: Action) {}
                    override fun addActions(vararg actions: Action) {}
                    override fun addStateProvider(stateProvider: StateProvider) {}
                    override fun reset() {}
                    override fun sleep(ticks: Int) {}
                },
                dummyBlackBoard,
                currentState
            )
        ) Status.SUCCESS else Status.FAILED
    }

    val totalCost: Float = actions.sumOf { it.cost.toDouble() }.toFloat()

    override fun toString(): String = "Plan(goal=${goal.name}, actions=${actions.map { it.name }})"
}
