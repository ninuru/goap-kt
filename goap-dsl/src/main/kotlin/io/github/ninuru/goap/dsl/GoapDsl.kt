package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.algorithms.AStarPlanner
import io.github.ninuru.goap.algorithms.ForwardPlanner

/**
 * Marker annotation for GOAP DSL receivers to prevent accidental access to outer scopes.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class GoapDsl

/**
 * Shorthand for creating an A* planner (primary planner choice).
 */
fun astar(): AStarPlanner = AStarPlanner()

/**
 * Shorthand for creating a forward search planner.
 */
fun forward(): ForwardPlanner = ForwardPlanner()

/**
 * Creates and configures a GOAP AI engine with sensible defaults.
 *
 * Example:
 * ```
 * val ai = goap {
 *     planner = forward()
 *     action("join_skyblock") {
 *         cost = 2f
 *         preconditions { "in_lobby" to true }
 *         effects { "in_skyblock" to true }
 *         onTick { _, _, _, _ -> SUCCESS }
 *     }
 *     goal("start_farming") {
 *         relevance = 2
 *         conditions { "in_garden" to true }
 *     }
 * }
 * ```
 */
fun goap(block: GoapSetup.() -> Unit): Goap {
    val setup = GoapSetup()
    block(setup)
    return setup.build()
}

/**
 * Configures an existing AI engine by adding actions, goals, and state providers.
 */
fun Goap.configure(block: GoapSetup.() -> Unit): Goap {
    val setup = GoapSetup(this)
    block(setup)
    setup.build()
    return this
}
