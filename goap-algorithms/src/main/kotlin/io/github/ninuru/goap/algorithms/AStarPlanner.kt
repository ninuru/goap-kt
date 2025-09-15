package io.github.ninuru.goap.algorithms

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.state.WorldState
import java.util.Optional
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicInteger

class AStarPlanner : PlannerAbstract() {

    override fun _plan(ai: Goap, goal: Goal, availableSortedActions: List<Action>): Plan? {
        val planner = Planner()
        return try {
            val worldState = ai.worldState
            val goalState = WorldStateFactory.fromConditions(goal.conditions)
            val plan = planner.plan(worldState, goalState, availableSortedActions)
            val result = Plan(goal, plan)
            result
        } catch (exception: NoPlanFoundException) {
            null
        }
    }

    private class NoPlanFoundException(message: String) : Throwable(message)

    private class Planner {
        private val lastId = AtomicInteger(0)
        private val open_ = PriorityQueue<Node>()
        private val closed_ = HashSet<Node>()
        private val openSet_ = HashMap<WorldState, Node>()
        private val closedSet_ = HashMap<WorldState, Node>()

        fun calculateHeuristic(now: WorldState, goal: WorldState): Int {
            return WorldStateFactory.delta(now, goal)
        }

        fun addToOpenList(n: Node) {
            open_.add(n)
            openSet_[n.ws] = n
        }

        fun popAndClose(): Node {
            val node = open_.poll()
            closed_.add(node)
            closedSet_[node.ws] = node
            openSet_.remove(node.ws)
            return node
        }

        fun memberOfClosed(ws: WorldState): Boolean {
            return closedSet_.containsKey(ws)
        }

        fun memberOfOpen(ws: WorldState): Optional<Node> {
            return Optional.ofNullable(openSet_[ws])
        }

        fun plan(start: WorldState, goal: WorldState, actions: List<Action>): List<Action> {
            if (start.covers(goal)) {
                throw NoPlanFoundException("Plan is already covered")
            }

            open_.clear()
            closed_.clear()

            val startingNode = Node(start, 0f, calculateHeuristic(start, goal).toFloat(), 0, null)
            open_.add(startingNode)

            while (open_.isNotEmpty()) {
                val current = popAndClose()

                if (current.ws.covers(goal)) {
                     val thePlan = mutableListOf<Action>()
                    var node = current
                    do {
                        thePlan.add(node.action!!)
                        val parentId = node.parentId
                        node = (open_.find { it.id == parentId } ?: closed_.find { it.id == parentId })!!
                    } while (node.parentId != 0)
                    thePlan.reverse()
                    return thePlan
                }

                for (potentialAction in actions) {
                    val preconditions = WorldStateFactory.fromBeliefs(potentialAction.preConditions)
                    if (!current.ws.covers(preconditions)) {
                        continue
                    }
                    val outcome = WorldStateFactory.merge(
                        current.ws,
                        potentialAction.effects,
                        potentialAction.sideEffects
                    )

                    if (memberOfClosed(outcome)) {
                        continue
                    }

                    val pOutcomeNode = memberOfOpen(outcome)
                    if (!pOutcomeNode.isPresent) {
                        val found = Node(
                            outcome,
                            current.g + potentialAction.cost,
                            calculateHeuristic(outcome, goal).toFloat(),
                            current.id,
                            potentialAction
                        )
                        addToOpenList(found)
                    } else {
                        val node = pOutcomeNode.get()
                        if (current.g + potentialAction.cost < node.g) {
                            node.parentId = current.id
                            node.g = current.g + potentialAction.cost
                            node.h = calculateHeuristic(outcome, goal).toFloat()
                            node.action = potentialAction
                        }
                    }
                }
            }

            throw NoPlanFoundException("A* planner could not find a path from start to goal")
        }

        private inner class Node(
             val ws: WorldState,
             var g: Float,
             var h: Float,
             var parentId: Int,
             var action: Action?
         ) : Comparable<Node> {
            val id: Int = lastId.incrementAndGet()

            fun f(): Float = g + h

            override fun compareTo(other: Node): Int = f().compareTo(other.f())

            override fun toString(): String {
                return "Node { id:$id parent:$parentId F:${f()} G:$g H:$h, $ws }"
            }
        }
    }
}

