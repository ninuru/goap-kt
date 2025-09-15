package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario

/**
 * Creates a scenario with a set of dynamically provided goals.
 *
 * Example:
 * ```
 * val farmingScenario = scenario {
 *     goal("farm_wheat") { ... }
 *     goal("sell_crops") { ... }
 * }
 * ```
 */
fun scenario(configure: ScenarioScope.() -> Unit): Scenario {
    val scope = ScenarioScope()
    scope.configure()
    return scope.build()
}

/**
 * DSL receiver for building a scenario (dynamic goal set).
 */
@GoapDsl
class ScenarioScope {
    private val goals = mutableListOf<Goal>()

    /**
     * Adds an inline goal definition to the scenario.
     */
    fun goal(name: String, configure: GoalScope.() -> Unit) {
        val goalScope = GoalScope(name)
        goalScope.configure()
        goals.add(goalScope.build())
    }

    /**
     * Adds a pre-built goal to the scenario.
     */
    fun goal(goal: Goal) {
        goals.add(goal)
    }

    internal fun build(): Scenario {
        val goalsList = goals.toList()
        return object : Scenario {
            override val goals: List<Goal> = goalsList
        }
    }
}
