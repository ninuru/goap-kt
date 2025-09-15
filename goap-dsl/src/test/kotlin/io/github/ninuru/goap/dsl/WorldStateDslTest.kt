package io.github.ninuru.goap.dsl

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorldStateDslTest {

    @Test
    fun testWorldStateInfixSyntax() {
        val ai = goap {
            worldState {
                "key1" to true
                "key2" to false
                "key3" to true
            }
        }

        assertTrue(ai.worldState.get("key1"))
        assertFalse(ai.worldState.get("key2"))
        assertTrue(ai.worldState.get("key3"))
    }

    @Test
    fun testWorldStateUnaryPlus() {
        val ai = goap {
            worldState {
                +"enabled"
                +"active"
            }
        }

        assertTrue(ai.worldState.get("enabled"))
        assertTrue(ai.worldState.get("active"))
    }

    @Test
    fun testWorldStateUnaryMinus() {
        val ai = goap {
            worldState {
                -"disabled"
                -"inactive"
            }
        }

        assertFalse(ai.worldState.get("disabled"))
        assertFalse(ai.worldState.get("inactive"))
    }

    @Test
    fun testWorldStateMixed() {
        val ai = goap {
            worldState {
                "manual" to true
                +"shorthand_true"
                -"shorthand_false"
                "another" to false
            }
        }

        assertTrue(ai.worldState.get("manual"))
        assertTrue(ai.worldState.get("shorthand_true"))
        assertFalse(ai.worldState.get("shorthand_false"))
        assertFalse(ai.worldState.get("another"))
    }

    @Test
    fun testWorldStateAfterAction() {
        val ai = goap {
            worldState {
                "start" to true
            }
            action("modifier") {
                effects {
                    "modified" to true
                    "start" to false
                }
            }
        }

        assertTrue(ai.worldState.get("start"))
        assertFalse(ai.worldState.get("modified"))
    }

}
