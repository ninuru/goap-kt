package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.action.ActionBuilder
import io.github.ninuru.goap.action.ActionCost
import io.github.ninuru.goap.action.ActionStrategy
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Availability
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

/**
 * DSL receiver for defining an action inline.
 *
 * Example:
 * ```
 * action("join_skyblock") {
 *     cost = 2f
 *     preconditions { "in_lobby" to true }
 *     effects { "in_skyblock" to true }
 *     onTick { ai, bb, ws, ticks -> SUCCESS }
 * }
 * ```
 */
@GoapDsl
class ActionScope(val name: String) {
    var cost: Float = ActionCost.NORMAL.cost
    var interruptible: Boolean = true
    var timeout: Int = 0

    private var preConditions = mutableSetOf<StateBelief>()
    private var runtimeConditions = mutableSetOf<StateBelief>()
    private var effects = mutableSetOf<StateBelief>()
    private var sideEffects = mutableSetOf<StateBelief>()

    private var strategy: ActionStrategy? = null
    private var onTick: ((Goap, BlackBoard, WorldState, Int) -> Status)? = null
    private var onInit: ((Goap, BlackBoard, WorldState) -> Unit)? = null
    private var onCleanup: ((Goap, BlackBoard, WorldState, Status, Int) -> Unit)? = null
    private var completedWhen: ((Goap, BlackBoard, WorldState) -> Boolean)? = null
    private var availableWhen: ((Goap, BlackBoard, WorldState) -> Boolean)? = null
    private var possibleWhen: ((Goap, BlackBoard, WorldState) -> Boolean)? = null

    /**
     * Sets the cost using an enum value: `cost(ActionCost.LOW)`.
     */
    fun cost(cost: ActionCost) {
        this.cost = cost.cost
    }

    /**
     * Defines preconditions as a belief set.
     */
    fun preconditions(configure: BeliefSetScope.() -> Unit) {
        val scope = BeliefSetScope()
        scope.configure()
        preConditions = scope.build().toMutableSet()
    }

    /**
     * Defines effects as a belief set.
     */
    fun effects(configure: BeliefSetScope.() -> Unit) {
        val scope = BeliefSetScope()
        scope.configure()
        effects = scope.build().toMutableSet()
    }

    /**
     * Defines runtime conditions (conditions checked during action execution).
     */
    fun runtimeConditions(configure: BeliefSetScope.() -> Unit) {
        val scope = BeliefSetScope()
        scope.configure()
        runtimeConditions = scope.build().toMutableSet()
    }

    /**
     * Defines side effects (effects that may occur but are not guaranteed).
     */
    fun sideEffects(configure: BeliefSetScope.() -> Unit) {
        val scope = BeliefSetScope()
        scope.configure()
        sideEffects = scope.build().toMutableSet()
    }

    /**
     * Sets an explicit action strategy.
     */
    fun strategy(strategy: ActionStrategy) {
        this.strategy = strategy
    }

    /**
     * Defines a compound strategy inline.
     */
    fun strategy(configure: CompoundStrategyScope.() -> Unit) {
        val scope = CompoundStrategyScope()
        scope.configure()
        this.strategy = scope.build()
    }

    /**
     * Defines the tick callback (main action logic).
     */
    fun onTick(block: (Goap, BlackBoard, WorldState, Int) -> Status) {
        this.onTick = block
    }

    /**
     * Defines the init callback (called when action starts).
     */
    fun onInit(block: (Goap, BlackBoard, WorldState) -> Unit) {
        this.onInit = block
    }

    /**
     * Defines the cleanup callback (called when action ends).
     */
    fun onCleanup(block: (Goap, BlackBoard, WorldState, Status, Int) -> Unit) {
        this.onCleanup = block
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
     * Defines possibility check logic.
     */
    fun possibleWhen(block: (Goap, BlackBoard, WorldState) -> Boolean) {
        this.possibleWhen = block
    }

    internal fun build(): Action {
        val builder = ActionBuilder(name)
            .cost(cost)
            .interruptible(interruptible)
            .timeout(timeout)

        for (pre in preConditions) builder.addPreCondition(pre)
        for (runtime in runtimeConditions) builder.addRuntimeCondition(runtime)
        for (effect in effects) builder.addEffect(effect)
        for (side in sideEffects) builder.addSideEffect(side)

        if (onInit != null) builder.onInit(onInit!!)
        if (onTick != null) builder.onTick(onTick!!)
        if (onCleanup != null) builder.onCleanup(onCleanup!!)
        if (completedWhen != null) builder.withComplete { ai, bb, ws -> completedWhen!!(ai, bb, ws) }
        if (availableWhen != null) builder.withAvailability(Availability { ai, bb, ws -> availableWhen!!(ai, bb, ws) })

        if (strategy != null) builder.withStrategy(strategy!!)

        return builder.build()
    }
}
