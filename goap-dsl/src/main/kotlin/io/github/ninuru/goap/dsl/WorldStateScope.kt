package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.state.WorldState

/**
 * DSL receiver for initializing world state with belief values.
 *
 * Example:
 * ```
 * worldState {
 *     "in_hypixel" to true
 *     "in_lobby" to true
 *     +"has_axe"  // shorthand for "has_axe" to true
 *     -"in_garden"  // shorthand for "in_garden" to false
 * }
 * ```
 */
@GoapDsl
class WorldStateScope(private val worldState: WorldState) {

    /**
     * Sets a world state belief with infix notation: `"in_lobby" to true`.
     */
    infix fun String.to(value: Boolean) {
        worldState.set(this, value)
    }

    /**
     * Shorthand for setting a belief to true: `+"in_lobby"`.
     */
    operator fun String.unaryPlus() {
        worldState.set(this, true)
    }

    /**
     * Shorthand for setting a belief to false: `-"in_lobby"`.
     */
    operator fun String.unaryMinus() {
        worldState.set(this, false)
    }
}
