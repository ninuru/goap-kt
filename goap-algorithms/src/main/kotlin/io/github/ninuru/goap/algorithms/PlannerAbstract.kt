package io.github.ninuru.goap.algorithms

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.action.ActionSorter
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.planner.Planner
import io.github.ninuru.goap.state.WorldStateFactory

abstract class PlannerAbstract : Planner {
    private val filter: GoalActionFilter = GoalActionFilterImpl()

    override fun plan(ai: Goap): Plan? {
        return plan(ai, ai.getGoals())
    }

    override fun plan(ai: Goap, goals: List<Goal>): Plan? {
        val actions = ai.availableActions
        for (eachGoal in goals) {
            val plan = plan(ai, eachGoal, actions)
            if (plan != null) {
                return plan
            }
        }
        return null
    }

    override fun plan(ai: Goap, goal: Goal): Plan? {
        return plan(ai, goal, ai.availableActions)
    }

    fun plan(ai: Goap, goal: Goal, availableActions: List<Action>): Plan? {
        val start = System.currentTimeMillis()
        var success = true
        return try {
            val filteredActions = filter.applyFilter(
                ai.worldState,
                WorldStateFactory.fromConditions(goal.conditions),
                availableActions
            )
            val sortedActions = filteredActions.sortedWith(ActionSorter())
            _plan(ai, goal, sortedActions)
        } catch (exception: Exception) {
            success = false
            null
        } finally {
            val end = System.currentTimeMillis()
            val status = if (success) "Successful" else "Unsuccessful"
            ai.logger.info("Calculated $status Plan for Goal ${goal.name} in ${end - start}ms")
        }
    }

    protected abstract fun _plan(ai: Goap, goal: Goal, availableSortedActions: List<Action>): Plan?
}

