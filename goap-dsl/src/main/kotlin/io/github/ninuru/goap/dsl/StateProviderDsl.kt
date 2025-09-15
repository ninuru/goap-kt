package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateProvider
import io.github.ninuru.goap.state.WorldState

/**
 * Creates a state provider from a lambda function.
 *
 * Example:
 * ```
 * stateProvider { ai, bb, ws ->
 *     ws.set("in_hypixel", checkHypixelConnection())
 * }
 * ```
 */
fun stateProvider(update: (Goap, BlackBoard, WorldState) -> Unit): StateProvider {
    return object : StateProvider {
        override fun updateState(ai: Goap, bb: BlackBoard, ws: WorldState) {
            update(ai, bb, ws)
        }
    }
}
