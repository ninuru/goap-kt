package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.state.StateBelief

/**
 * DSL receiver for defining sets of beliefs (preconditions, effects, conditions, etc.).
 *
 * Example:
 * ```
 * preconditions {
 *     "in_lobby" to true
 *     "has_axe" to true
 * }
 * ```
 *
 * Supports shorthand syntax:
 * - `"key" to value` — explicit belief
 * - `+"key"` — equivalent to `"key" to true`
 * - `-"key"` — equivalent to `"key" to false`
 */
@GoapDsl
class BeliefSetScope {
    internal val beliefs = mutableSetOf<StateBelief>()

    /**
     * Adds a belief with infix notation: `"in_lobby" to true`.
     */
    infix fun String.to(value: Boolean) {
        beliefs.add(StateBelief.of(this, value))
    }

    /**
     * Shorthand for setting a belief to true: `+"in_lobby"` is equivalent to `"in_lobby" to true`.
     */
    operator fun String.unaryPlus() {
        beliefs.add(StateBelief.of(this, true))
    }

    /**
     * Shorthand for setting a belief to false: `-"in_lobby"` is equivalent to `"in_lobby" to false`.
     */
    operator fun String.unaryMinus() {
        beliefs.add(StateBelief.of(this, false))
    }

    /**
     * Adds a belief explicitly.
     */
    fun add(belief: StateBelief) {
        beliefs.add(belief)
    }

    internal fun build(): Set<StateBelief> = beliefs.toSet()
}
