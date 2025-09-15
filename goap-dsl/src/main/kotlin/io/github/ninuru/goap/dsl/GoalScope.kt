package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Availability
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.GoalBuilder
import io.github.ninuru.goap.goal.GoalRelevance
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

/**
 * DSL receiver for defining a goal inline.
 *
 * Example:
 * ```
 * goal("start_farming") {
 *     relevance = 2
 *     conditions { "in_garden" to true }
 *     completedWhen { ws, ai, bb -> ws.get("farming_done") }
 * }
 * ```
 */
@GoapDsl
class GoalScope(val name: String) {
    var relevance: Int = 0
    var interruptible: Boolean = true

    private var relevanceProvider: GoalRelevance? = null
    private var conditions = mutableSetOf<StateBelief>()
    private var completedWhen: ((Goap, BlackBoard, WorldState) -> Boolean)? = null
    private var availableWhen: ((Goap, BlackBoard, WorldState) -> Boolean)? = null
    private var onActivate: ((Goap, BlackBoard, WorldState) -> Unit)? = null
    private var onDeactivate: ((Goap, BlackBoard, WorldState) -> Unit)? = null

    /**
     * Sets a dynamic relevance provider.
     */
    fun relevance(provider: GoalRelevance) {
        this.relevanceProvider = provider
    }

    /**
     * Defines goal conditions as a belief set.
     */
    fun conditions(configure: BeliefSetScope.() -> Unit) {
        val scope = BeliefSetScope()
        scope.configure()
        conditions = scope.build().toMutableSet()
    }

    /**
     * Defines completion check logic.
     */
    fun completedWhen(block: (Goap, BlackBoard, WorldState) -> Boolean) {
        this.completedWhen = block
    }

    /**
     * Defines availability check logic.
     */
    fun availableWhen(block: (Goap, BlackBoard, WorldState) -> Boolean) {
        this.availableWhen = block
    }

    /**
     * Defines the activation callback (called when goal becomes active).
     */
    fun onActivate(block: (Goap, BlackBoard, WorldState) -> Unit) {
        this.onActivate = block
    }

    /**
     * Defines the deactivation callback (called when goal becomes inactive).
     */
    fun onDeactivate(block: (Goap, BlackBoard, WorldState) -> Unit) {
        this.onDeactivate = block
    }

    internal fun build(): Goal {
        val builder = GoalBuilder(name)
            .withRelevance(relevance)
            .interruptible(interruptible)

        for (condition in conditions) {
            builder.addCondition(condition)
        }

        if (relevanceProvider != null) {
            builder.withRelevance(relevanceProvider!!)
        }

        if (completedWhen != null) {
            builder.onCompleted { ai, bb, ws -> completedWhen!!(ai, bb, ws) }
        }

        if (availableWhen != null) {
            builder.withAvailability(Availability { ai, bb, ws -> availableWhen!!(ai, bb, ws) })
        }

        if (onActivate != null) {
            builder.onActivate { ai, bb, ws -> onActivate!!(ai, bb, ws) }
        }

        if (onDeactivate != null) {
            builder.onDeactivate { ai, bb, ws -> onDeactivate!!(ai, bb, ws) }
        }

        return builder.build()
    }
}
