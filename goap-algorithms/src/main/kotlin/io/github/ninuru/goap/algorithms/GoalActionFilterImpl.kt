package io.github.ninuru.goap.algorithms

import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import io.github.ninuru.goap.state.WorldStateFactory
import java.io.IOException

class GoalActionFilterImpl : GoalActionFilter {

    override fun applyFilter(current: WorldState, goal: WorldState, actions: List<Action>): Set<Action> {
        val indexedActions = indexActionsByEffect(actions)
        val result = mutableSetOf<Action>()
        if (!backtrackFilter(current, goal.clone(), indexedActions, null, result)) {
            throw IOException("Not possible")
        }
        return result
    }

    private fun backtrackFilter(
        current: WorldState,
        goal: WorldState,
        indexedActions: Map<StateBelief, List<Action>>,
        lastAction: Action?,
        result: MutableSet<Action>
    ): Boolean {
        val goalBeliefsToRemove = mutableSetOf<StateBelief>()
        for (belief in goal.props.keys) {
            if (current.get(belief)) {
                goalBeliefsToRemove.add(StateBelief.of(belief))
            }
        }
        
        if (current.covers(goal)) return true

        for (condition in goal.props.keys) {
            val belief = StateBelief.of(condition)
            val actions = indexedActions[belief] ?: emptyList()
            if (actions.isEmpty()) {
                return false
            }
            val foundActions = mutableSetOf<Action>()
            for (action in actions) {
                if (action == lastAction) continue

                val newGoal = WorldStateFactory.empty()
                action.preConditions.forEach { newGoal.set(it) }

                if (backtrackFilter(current, newGoal, indexedActions, action, foundActions)) {
                    foundActions.add(action)
                }
            }
            if (foundActions.isEmpty()) {
                return false
            }
            result.addAll(foundActions)
        }
        return true
    }

    private fun indexActionsByEffect(actions: Collection<Action>): Map<StateBelief, List<Action>> {
        val result = mutableMapOf<StateBelief, MutableList<Action>>()

        for (action in actions) {
            for (effect in action.effects) {
                if (effect.key.isNotEmpty() && effect.group?.isNotEmpty() == true) {
                    result.computeIfAbsent(StateBelief.groupOf(effect.group!!)) { mutableListOf() }.add(action)
                }
                result.computeIfAbsent(effect) { mutableListOf() }.add(action)
            }
        }

        return result
    }
}


