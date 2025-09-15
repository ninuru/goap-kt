package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.action.ActionCost
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActionDslTest {

    @Test
    fun testActionWithCost() {
        val ai = goap {
            action("expensive_action") {
                effects { "done" to true }
                cost(ActionCost.VERY_HIGH)
            }
        }

        val action = ai.actions[0]
        assertEquals(ActionCost.VERY_HIGH.cost, action.cost)
    }

    @Test
    fun testActionWithFloatCost() {
        val ai = goap {
            action("custom_cost") {
                effects { "done" to true }
                cost = 7.5f
            }
        }

        val action = ai.actions[0]
        assertEquals(7.5f, action.cost)
    }

    @Test
    fun testActionWithPreconditions() {
        val ai = goap {
            action("requires_items") {
                preconditions {
                    "has_axe" to true
                    "has_logs" to true
                }
            }
        }

        val action = ai.actions[0]
        assertEquals(2, action.preConditions.size)
        assertTrue(action.preConditions.any { it.key == "has_axe" && it.value })
        assertTrue(action.preConditions.any { it.key == "has_logs" && it.value })
    }

    @Test
    fun testActionWithEffects() {
        val ai = goap {
            action("craft_item") {
                effects {
                    "has_crafted" to true
                    "has_materials" to false
                }
            }
        }

        val action = ai.actions[0]
        assertEquals(2, action.effects.size)
        assertTrue(action.effects.any { it.key == "has_crafted" && it.value })
        assertTrue(action.effects.any { it.key == "has_materials" && !it.value })
    }

    @Test
    fun testActionWithRuntimeConditions() {
        val ai = goap {
            action("conditional") {
                effects { "done" to true }
                runtimeConditions {
                    "still_valid" to true
                }
            }
        }

        val action = ai.actions[0]
        assertEquals(1, action.runtimeConditions.size)
    }

     @Test
     fun testActionWithSideEffects() {
         val ai = goap {
             action("risky_action") {
                 effects { "done" to true }
                 sideEffects {
                     "might_break" to true
                 }
             }
         }

         val action = ai.actions[0]
         assertEquals(1, action.sideEffects.size)
     }

     @Test
     fun testActionWithCallbacks() {
         var initCalled = false
         var tickCalled = false
         var cleanupCalled = false

         val ai = goap {
             action("callbacks") {
                 effects { "done" to true }
                 onInit { _, _, _ ->
                     initCalled = true
                 }
                 onTick { _, _, _, _ ->
                     tickCalled = true
                     Status.SUCCESS
                 }
                 onCleanup { _, _, _, _, _ ->
                     cleanupCalled = true
                 }
             }
         }

         val action = ai.actions[0]
         action.init(ai, ai.blackBoard, ai.worldState)
         assertTrue(initCalled)

         action.tick(ai, ai.blackBoard, ai.worldState, 0)
         assertTrue(tickCalled)

         action.cleanup(ai, ai.blackBoard, ai.worldState, Status.SUCCESS, 1)
         assertTrue(cleanupCalled)
     }

    @Test
    fun testActionWithCompletion() {
        val ai = goap {
            action("completable") {
                effects { "task_done" to true }
                completedWhen { _, _, ws ->
                    ws.get("task_done")
                }
            }
        }

        val action = ai.actions[0]
        assertFalse(action.isCompleted(ai, ai.blackBoard, ai.worldState))

        ai.worldState.set("task_done", true)
        assertTrue(action.isCompleted(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    fun testActionWithAvailability() {
        val ai = goap {
            action("conditional_action") {
                effects { "acted" to true }
                availableWhen { _, _, ws ->
                    ws.get("can_act")
                }
            }
        }

        val action = ai.actions[0]
        assertFalse(action.isAvailable(ai, ai.blackBoard, ai.worldState))

        ai.worldState.set("can_act", true)
        assertTrue(action.isAvailable(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    fun testActionInterruptible() {
        val ai = goap {
            action("non_interruptible") {
                effects { "done" to true }
                interruptible = false
            }
        }

        val action = ai.actions[0]
        assertFalse(action.isInterruptible)
    }

    @Test
    fun testActionTimeout() {
        val ai = goap {
            action("timeout_action") {
                effects { "done" to true }
                timeout = 100
            }
        }

        val action = ai.actions[0]
        assertEquals(100, action.timeout)
    }

     @Test
     fun testMultipleActionsRegistration() {
         val ai = goap {
             action("action1") {
                 effects { "done1" to true }
                 onTick { _, _, _, _ -> Status.SUCCESS }
             }
             action("action2") {
                 effects { "done2" to true }
                 onTick { _, _, _, _ -> Status.SUCCESS }
             }
             action("action3") {
                 effects { "done3" to true }
                 onTick { _, _, _, _ -> Status.SUCCESS }
             }
         }

         assertEquals(3, ai.actions.size)
     }

     @Test
     fun testClassBasedActionRegistration() {
         val customAction = object : Action {
             override val name = "custom"
             override val effects = setOf(StateBelief.of("done", true))
             override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState) = true
             override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}
             override fun isPossible(ai: Goap, bb: BlackBoard, ws: WorldState) = true
             override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {}
             override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int) = Status.SUCCESS
         }

        val ai = goap {
            action(customAction)
        }

        assertEquals(1, ai.actions.size)
        assertEquals("custom", ai.actions[0].name)
    }
}
