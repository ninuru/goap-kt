package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.ActionStrategy
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.WorldState

/**
 * Creates a compound strategy with multiple sequential steps.
 *
 * Example:
 * ```
 * strategy {
 *     step("step1") { _, _, _, _ -> RUNNING }
 *     step("step2") { _, _, _, _ -> SUCCESS }
 * }
 * ```
 */
fun compoundStrategy(configure: CompoundStrategyScope.() -> Unit): ActionStrategy {
    val scope = CompoundStrategyScope()
    scope.configure()
    return scope.build()
}

/**
 * DSL receiver for building a compound strategy (multiple sequential steps).
 */
@GoapDsl
class CompoundStrategyScope {
    private val strategies = mutableListOf<Pair<String, ActionStrategy>>()

    /**
     * Adds a named strategy step.
     */
    fun step(name: String, strategy: ActionStrategy) {
        strategies.add(name to strategy)
    }

    /**
     * Adds a named strategy step with a tick lambda.
     */
    fun step(name: String, tick: (Goap, BlackBoard, WorldState, Int) -> Status) {
        val strategy = object : ActionStrategy {
            override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}
            override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
                return tick(ai, bb, ws, ticksInAction)
            }
            override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {}
        }
        strategies.add(name to strategy)
    }

    internal fun build(): ActionStrategy {
        return io.github.ninuru.goap.action.ActionStrategy.compound()
            .apply {
                for ((name, strategy) in strategies) {
                    then(name, strategy)
                }
            }
            .build()
    }
}
