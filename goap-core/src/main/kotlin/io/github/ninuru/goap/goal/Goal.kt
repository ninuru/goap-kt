package io.github.ninuru.goap.goal

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

fun interface GoalRelevance {
    fun getRelevance(ai: Goap, bb: BlackBoard, ws: WorldState): Int
}

fun interface Availability {
    fun canBeActivated(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean
}

interface Goal {
    val name: String
    fun getRelevance(ai: Goap, bb: BlackBoard, ws: WorldState): Int = 0
    val isInterruptible: Boolean get() = true
    val conditions: Set<StateBelief> get() = emptySet()
    fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean
    fun canBeActivated(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean = true
    fun activate(ai: Goap, bb: BlackBoard, ws: WorldState)
    fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState)

    companion object {
        fun builder(name: String): GoalBuilder = GoalBuilder(name)

        val IMPOSSIBLE = object : Goal {
            override val name = "ImpossibleGoal"
            override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState) = false
            override val conditions = setOf(StateBelief.NEVER)
            override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
            override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
        }
    }
}

interface Scenario {
    val goals: List<Goal>

    companion object {
        val NONE = object : Scenario {
            override val goals = emptyList<Goal>()
        }
    }
}
