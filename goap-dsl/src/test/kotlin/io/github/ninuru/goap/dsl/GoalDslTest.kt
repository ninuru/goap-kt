package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.GoalRelevance
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoalDslTest {

    @Test
    fun testGoalWithRelevance() {
        val ai = goap {
            goal("important_goal") {
                relevance = 10
            }
        }

        val goal = ai.getGoals()[0]
        assertEquals(10, goal.getRelevance(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    fun testGoalWithConditions() {
        val ai = goap {
            goal("conditional_goal") {
                conditions {
                    "condition1" to true
                    "condition2" to false
                }
            }
        }

        val goal = ai.getGoals()[0]
        assertEquals(2, goal.conditions.size)
        assertTrue(goal.conditions.any { it.key == "condition1" && it.value })
        assertTrue(goal.conditions.any { it.key == "condition2" && !it.value })
    }

    @Test
    fun testGoalWithCompletion() {
        val ai = goap {
            goal("completable_goal") {
                completedWhen { _, _, ws ->
                    ws.get("goal_state")
                }
            }
        }

        val goal = ai.getGoals()[0]
        assertFalse(goal.isCompleted(ai, ai.blackBoard, ai.worldState))

        ai.worldState.set("goal_state", true)
        assertTrue(goal.isCompleted(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    fun testGoalWithAvailability() {
        val ai = goap {
            worldState {
                "can_activate" to false
            }
            goal("available_goal") {
                relevance = 1
                availableWhen { _, _, ws ->
                    ws.get("can_activate")
                }
            }
        }

        assertEquals(0, ai.getGoals().size)

        ai.worldState.set("can_activate", true)
        assertEquals(1, ai.getGoals().size)
    }

    @Test
    fun testGoalWithActivationCallbacks() {
        var activateCalled = false
        var deactivateCalled = false

        val ai = goap {
            goal("callback_goal") {
                onActivate { _, _, _ ->
                    activateCalled = true
                }
                onDeactivate { _, _, _ ->
                    deactivateCalled = true
                }
            }
        }

        val goal = ai.getGoals()[0]
        goal.activate(ai, ai.blackBoard, ai.worldState)
        assertTrue(activateCalled)

        goal.deactivate(ai, ai.blackBoard, ai.worldState)
        assertTrue(deactivateCalled)
    }

    @Test
    fun testGoalInterruptible() {
        val ai = goap {
            goal("non_interruptible") {
                interruptible = false
            }
        }

        val goal = ai.getGoals()[0]
        assertFalse(goal.isInterruptible)
    }

    @Test
    fun testDynamicRelevance() {
        val ai = goap {
            goal("dynamic_goal") {
                relevance(GoalRelevance { _, _, ws ->
                    if (ws.get("boost")) 20 else 5
                })
            }
        }

        val goal = ai.getGoals()[0]
        assertEquals(5, goal.getRelevance(ai, ai.blackBoard, ai.worldState))

        ai.worldState.set("boost", true)
        assertEquals(20, goal.getRelevance(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    fun testMultipleGoalsRegistration() {
        val ai = goap {
            goal("goal1") { relevance = 1 }
            goal("goal2") { relevance = 2 }
            goal("goal3") { relevance = 3 }
        }

        assertEquals(3, ai.getGoals().size)
    }

    @Test
    fun testGoalBeliefSetShorthand() {
        val ai = goap {
            goal("shorthand_goal") {
                conditions {
                    +"enabled"
                    -"disabled"
                }
            }
        }

        val goal = ai.getGoals()[0]
        assertEquals(2, goal.conditions.size)
    }

      @Test
      fun testClassBasedGoalRegistration() {
          val customGoal = object : Goal {
              override val name = "custom"
              override val conditions = setOf(StateBelief.of("done", true))
              override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState) = false
              override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
              override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
          }

          val ai = goap {
              goal(customGoal)
          }

          assertEquals(1, ai.getGoals().size)
          assertEquals("custom", ai.getGoals()[0].name)
      }
}
