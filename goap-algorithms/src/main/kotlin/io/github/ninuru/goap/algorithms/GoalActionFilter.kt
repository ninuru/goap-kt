package io.github.ninuru.goap.algorithms

import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.state.WorldState
import java.io.IOException

fun interface GoalActionFilter {
    /**
     * Applies the filter to the goal world state, removing any action that does
     * not help in reaching the goal. Also checks if the goal can be reached at all.
     *
     * @param current current world state
     * @param goal desired world state
     * @param actions actions we could do at this time
     * @return set of actions that contribute to our goal
     * @throws IOException if the goal cannot be reached
     */
    fun applyFilter(current: WorldState, goal: WorldState, actions: List<Action>): Set<Action>
}
