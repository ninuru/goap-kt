package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Status
import io.github.ninuru.goap.fsm.ErrorHandler
import io.github.ninuru.goap.fsm.FSMState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FsmDslTest {

    @Test
    fun testFsmStateWithCallbacks() {
        var initCalled = false
        var tickCalled = false
        var endCalled = false

        val state = fsmState {
            onInit { initCalled = true }
            onTick {
                tickCalled = true
                FSMState.END
            }
            onEnd { endCalled = true }
        }

        state.onInit()
        assertTrue(initCalled)

        val nextState = state.onTick()
        assertTrue(tickCalled)
        assertNull(nextState)

        state.onEnd()
        assertTrue(endCalled)
    }

    @Test
    fun testFsmWithInitialState() {
        val fsm = fsm {
            initialState(fsmState {
                onTick { FSMState.END }
            })
        }

        assertNotNull(fsm)
    }

    @Test
    fun testFsmWithInlineInitialState() {
        val fsm = fsm {
            initialState {
                onTick { FSMState.END }
            }
        }

        assertNotNull(fsm)
    }

    @Test
    fun testFsmStrategy() {
        val strategy = fsmStrategy {
            initialState {
                onTick { FSMState.END }
            }
        }

        assertNotNull(strategy)
    }

    @Test
    fun testFsmInAction() {
        val ai = goap {
            action("fsm_action") {
                effects { "fsm_done" to true }
                strategy(fsmStrategy {
                    initialState {
                        onInit { println("FSM started") }
                        onTick { FSMState.END }
                    }
                })
            }
        }

        assertNotNull(ai.actions[0])
    }

    @Test
    fun testFsmStateTransition() {
        var firstCalled = false
        var secondCalled = false

        val secondState = fsmState {
            onInit { secondCalled = true }
            onTick { FSMState.END }
        }

        val firstState = fsmState {
            onInit { firstCalled = true }
            onTick { secondState }
        }

        firstState.onInit()
        assertTrue(firstCalled)

        val nextState = firstState.onTick()
        assertNotNull(nextState)
        nextState?.onInit()
        assertTrue(secondCalled)
    }

    @Test
    fun testFsmWithDelay() {
        val fsm = fsm {
            minDelay = 5
            maxDelay = 10
            initialState {
                onTick { FSMState.END }
            }
        }

        assertNotNull(fsm)
    }

    @Test
    fun testFsmWithErrorHandler() {
        var errorHandled = false

        val fsm = fsm {
            errorHandler = ErrorHandler { _ ->
                errorHandled = true
            }
            initialState {
                onTick { FSMState.END }
            }
        }

        assertNotNull(fsm)
    }

    @Test
    fun testFsmTick() {
        var tickCount = 0
        var state : FSMState?? = null

        state = fsmState {
            onTick {
                tickCount++
                if (tickCount < 3) state else FSMState.END
            }
        }

        val fsm = fsm {
            initialState(state!!)
        }

        fsm.activate()

        val mockAI = goap { }
        var status = fsm.tick(mockAI, mockAI.blackBoard, mockAI.worldState)
        assertEquals(Status.RUNNING, status)

        status = fsm.tick(mockAI, mockAI.blackBoard, mockAI.worldState)
        assertEquals(Status.RUNNING, status)
    }

}
