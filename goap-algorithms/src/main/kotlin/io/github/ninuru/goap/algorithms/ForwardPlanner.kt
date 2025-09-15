package io.github.ninuru.goap.algorithms

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class ForwardPlanner : PlannerAbstract() {

    override fun _plan(ai: Goap, goal: Goal, availableSortedActions: List<Action>): Plan? {
        ai.logger.debug("Planning goal $goal")
        val orderedActions = availableSortedActions.toMutableList()
         val goalNode = Node(null, null, goal.conditions.toMutableSet(), 0.0)

         if (findPath(ai.worldState.clone(), goalNode, orderedActions, null)) {
             if (goalNode.isLeafDead()) return null

             val actionList = mutableListOf<Action>()
             val actionStack = ArrayDeque<Action>()

            var current = goalNode
            while (current.leaves.isNotEmpty()) {
                val cheapestLeaf = current.leaves.minByOrNull { it.cost }
                    ?: throw RuntimeException("No leaf found")
                current = cheapestLeaf
                actionStack.addLast(cheapestLeaf.action!!)
            }

            while (actionStack.isNotEmpty()) {
                actionList.add(actionStack.removeLast())
            }

            return Plan(goal, actionList)
        }
        return null
    }

     private fun findPath(
         currentState: WorldState,
         parent: Node,
         actions: List<Action>,
         lastAction: Action?
     ): Boolean {
        for (action in actions) {
            if (action == lastAction) {
                continue
            }

            val requiredEffects = parent.requiredEffects.toMutableSet()
            requiredEffects.removeAll { currentState.match(it) }

            if (requiredEffects.isEmpty()) {
                return true
            }

            if (action.effects.any { requiredEffects.contains(it) }) {
                val newRequiredEffects = requiredEffects.toMutableSet()
                newRequiredEffects.removeAll(action.effects)
                newRequiredEffects.addAll(action.preConditions)

                val newNode = Node(parent, action, newRequiredEffects, parent.cost + action.cost)

                if (findPath(currentState, newNode, actions, action)) {
                    parent.leaves.add(newNode)
                    newRequiredEffects.removeAll(newNode.action!!.preConditions)
                }

                if (newRequiredEffects.isEmpty()) {
                    return true
                }
            }
        }

        return false
    }

      private class Node(
          val parent: Node?,
          val action: Action?,
          val requiredEffects: MutableSet<StateBelief>,
          val cost: Double
      ) {
         val leaves = mutableListOf<Node>()

         fun isLeafDead(): Boolean = leaves.isEmpty() && action == null
     }
 }
