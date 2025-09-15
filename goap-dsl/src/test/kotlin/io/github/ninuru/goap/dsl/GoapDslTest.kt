package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.ActionCost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoapDslTest {

    @Test
    fun testCreateEngineWithDefaults() {
        val ai = goap {
            action("test_action") {
                cost = 2f
                preconditions { "ready" to true }
                effects { "done" to true }
                onTick { _, _, _, _ -> Status.SUCCESS }
            }
            goal("test_goal") {
                relevance = 5
                conditions { "done" to true }
            }
        }

        assertNotNull(ai)
        assertEquals(1, ai.actions.size)
        assertEquals(1, ai.getGoals().size)
        assertEquals("test_action", ai.actions[0].name)
        assertEquals("test_goal", ai.getGoals()[0].name)
    }

      @Test
      fun testOverrideDefaults() {
          val ai = goap {
              planner = forward()
              action("simple") {
                  effects { "done" to true }
                  onTick { _, _, _, _ -> Status.SUCCESS }
              }
          }

         assertNotNull(ai)
         assertEquals(1, ai.actions.size)
     }

    @Test
    fun testWorldStateInitialization() {
        val ai = goap {
            worldState {
                "in_lobby" to true
                "has_axe" to false
                +"in_garden"
            }
        }

        assertNotNull(ai)
        assertTrue(ai.worldState.get("in_lobby"))
        assertFalse(ai.worldState.get("has_axe"))
        assertTrue(ai.worldState.get("in_garden"))
    }

    @Test
    fun testAddStateProvider() {
        val ai = goap {
            stateProvider { ai, bb, ws ->
                ws.set("provider_set", true)
            }
        }

        assertNotNull(ai)
        ai.tick()
        assertTrue(ai.worldState.get("provider_set"))
    }

    @Test
    fun testSetScenario() {
        val testScenario = scenario {
            goal("scenario_goal") {
                relevance = 3
                conditions { "target" to true }
            }
        }

        val ai = goap {
            scenario(testScenario)
        }

        assertNotNull(ai)
        assertEquals(1, ai.scenario.goals.size)
    }

     @Test
     fun testConfigureExistingAI() {
         val ai = goap {
             action("first") {
                 effects { "done1" to true }
                 onTick { _, _, _, _ -> Status.SUCCESS }
             }
         }

         ai.configure {
             action("second") {
                 effects { "done2" to true }
                 onTick { _, _, _, _ -> Status.SUCCESS }
             }
         }

         assertEquals(2, ai.actions.size)
     }

    @Test
    fun testActionWithPreconditionsAndEffects() {
        val ai = goap {
            action("join_skyblock") {
                cost(ActionCost.LOW)
                preconditions {
                    "in_hypixel" to true
                    "in_lobby" to true
                }
                effects {
                    "in_skyblock" to true
                    "in_lobby" to false
                }
                onTick { _, _, _, _ -> Status.SUCCESS }
            }
        }

        val action = ai.actions[0]
        assertEquals("join_skyblock", action.name)
        assertEquals(ActionCost.LOW.cost, action.cost)
        assertEquals(2, action.preConditions.size)
        assertEquals(2, action.effects.size)
    }

    @Test
    fun testGoalWithRelevance() {
        val ai = goap {
            goal("farming") {
                relevance = 10
                conditions { "in_garden" to true }
                completedWhen { _, _, ws ->
                    ws.get("farming_done")
                }
            }
        }

         val goal = ai.getGoals()[0]
         assertEquals("farming", goal.name)
         assertEquals(10, goal.getRelevance(ai, ai.blackBoard, ai.worldState))
    }

     @Test
     fun testActionWithCompoundStrategy() {
         val ai = goap {
             action("multi_step") {
                 effects { "done" to true }
                 strategy {
                     step("step1") { _, _, _, _ -> Status.RUNNING }
                     step("step2") { _, _, _, _ -> Status.SUCCESS }
                 }
             }
         }

         assertNotNull(ai.actions[0])
         assertEquals(1, ai.actions.size)
     }

}
