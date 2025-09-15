package io.github.ninuru.goap.test.state

import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorldStateFactoryTest {
    private lateinit var state1: WorldState
    private lateinit var state2: WorldState

    @BeforeEach
    fun setUp() {
        state1 = WorldStateFactory.empty()
        state2 = WorldStateFactory.empty()

        state1.set("key1", true)
        state1.set("key2", false)

        state2.set("key2", true)
        state2.set("key3", true)
    }

    @Test
    fun testFromEffects() {
        val effects = setOf(
            StateBelief.of("key1", true),
            StateBelief.of("key2", false)
        )

        val state = WorldStateFactory.fromEffects(effects)
        assertTrue(state.get("key1"))
        assertFalse(state.get("key2"))
    }

    @Test
    fun testMerge() {
        val mergedState = WorldStateFactory.merge(state1, state2)

        assertTrue(mergedState.get("key1"))
        assertTrue(mergedState.get("key2"))
        assertTrue(mergedState.get("key3"))
    }

    @Test
    fun testDelta() {
        val deltaState = WorldStateFactory.difference(state1, state2)

        assertFalse(deltaState.get("key1"))
        assertTrue(deltaState.get("key2"))
        assertTrue(deltaState.get("key3"))
    }

     @Test
     fun testDelta2() {
         val startState = WorldStateFactory.empty()
         startState.set("test", true)

         val goalState = WorldStateFactory.empty()
         goalState.set("test", false)

         val difference = WorldStateFactory.difference(startState, goalState)
         assertEquals(1, difference.size)
     }

    @Test
    fun testHeuristic() {
        val startState1 = WorldStateFactory.empty()
        startState1.set("goal_state_1", true)

        val startState2 = WorldStateFactory.empty()
        startState2.set("goal_state_2", true)

        val goalState = WorldStateFactory.empty()
        goalState.set("goal_state_1", true)
        goalState.set("goal_state_2", true)
        goalState.set("goal_state_3", true)

        var delta1 = WorldStateFactory.delta(startState1, goalState)
        var delta2 = WorldStateFactory.delta(startState2, goalState)

        assertEquals(delta1, delta2)

        startState2.set("random_value", false)
        delta1 = WorldStateFactory.delta(startState1, goalState)
        delta2 = WorldStateFactory.delta(startState2, goalState)
        assertEquals(delta1, delta2)

        startState1.set("goal_state_3", true)
        delta1 = WorldStateFactory.delta(startState1, goalState)
        delta2 = WorldStateFactory.delta(startState2, goalState)
        assertTrue(delta1 < delta2)
    }
}
