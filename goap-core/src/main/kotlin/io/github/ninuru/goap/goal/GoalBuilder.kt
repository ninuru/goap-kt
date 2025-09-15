package io.github.ninuru.goap.goal

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class GoalBuilder(private val name: String) {
    private var relevance: Int = 0
    private var relevanceProvider: GoalRelevance? = null
    private var interruptible: Boolean = true
    private var conditions = mutableSetOf<StateBelief>()
    private var availability: Availability? = null
    private var onActivate: ((Goap, BlackBoard, WorldState) -> Unit)? = null
    private var onDeactivate: ((Goap, BlackBoard, WorldState) -> Unit)? = null
    private var onCompleted: ((Goap, BlackBoard, WorldState) -> Boolean)? = null

    fun withRelevance(value: Int) = apply { this.relevance = value }
    fun withRelevance(provider: GoalRelevance) = apply { this.relevanceProvider = provider }
    fun interruptible(value: Boolean) = apply { this.interruptible = value }
    fun addCondition(key: String, value: Boolean = true) = apply { conditions.add(StateBelief.of(key, value)) }
    fun addCondition(belief: StateBelief) = apply { conditions.add(belief) }
    fun withAvailability(availability: Availability) = apply { this.availability = availability }
    fun onActivate(block: (Goap, BlackBoard, WorldState) -> Unit) = apply { this.onActivate = block }
    fun onDeactivate(block: (Goap, BlackBoard, WorldState) -> Unit) = apply { this.onDeactivate = block }
    fun onCompleted(block: (Goap, BlackBoard, WorldState) -> Boolean) = apply { this.onCompleted = block }

    fun build(): Goal = GoalBuilt(
        name = name,
        relevance = relevance,
        relevanceProvider = relevanceProvider,
        isInterruptible = interruptible,
        conditions = conditions.toSet(),
        availability = availability,
        onActivate = onActivate,
        onDeactivate = onDeactivate,
        onCompleted = onCompleted
    )
}

class GoalBuilt(
    override val name: String,
    private val relevance: Int,
    private val relevanceProvider: GoalRelevance?,
    override val isInterruptible: Boolean,
    override val conditions: Set<StateBelief>,
    private val availability: Availability?,
    private val onActivate: ((Goap, BlackBoard, WorldState) -> Unit)?,
    private val onDeactivate: ((Goap, BlackBoard, WorldState) -> Unit)?,
    private val onCompleted: ((Goap, BlackBoard, WorldState) -> Boolean)?
) : Goal {
    override fun getRelevance(ai: Goap, bb: BlackBoard, ws: WorldState): Int =
        relevanceProvider?.getRelevance(ai, bb, ws) ?: relevance
    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean =
        onCompleted?.invoke(ai, bb, ws) ?: false
    override fun canBeActivated(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean =
        availability?.canBeActivated(ai, bb, ws) ?: true
    override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {
        onActivate?.invoke(ai, bb, ws)
    }
    override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {
        onDeactivate?.invoke(ai, bb, ws)
    }
}
