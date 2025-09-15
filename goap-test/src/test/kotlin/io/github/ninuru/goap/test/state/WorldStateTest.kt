package io.github.ninuru.goap.test.state

import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.state.StateKey
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorldStateTest {
    private lateinit var state: WorldState

    @BeforeEach
    fun setUp() {
        state = WorldStateFactory.empty()
    }

    @Test
    fun testGroup() {
        state.set(StateKey.of("hello1", "hello"), true)
        state.set(StateKey.of("hello2", "hello"), false)

        Assertions.assertTrue(state.get("hello1"))
        Assertions.assertFalse(state.get("hello2"))
    }

    @Test
    fun testGroupClone() {
        state.set(StateKey.groupOf("hello"), true)
        state.set(StateKey.of("hello1", "hello"), true)
        state.set(StateKey.of("hello2", "hello"), false)

        Assertions.assertEquals(state, state.clone())
    }

    @Test
    fun testSetWSProps() {
        val anotherStateMap = WorldStateFactory.empty()
        anotherStateMap.set("key1", true)
        anotherStateMap.set("key2", false)

        state.setAll(anotherStateMap)

        Assertions.assertTrue(state.get("key1"))
        Assertions.assertFalse(state.get("key2"))
    }

    @Test
    fun testClone() {
        state.set("key1", true)
        val clonedState = state.clone()
        Assertions.assertEquals(state, clonedState)
    }

    @Test
    fun testCovers() {
        val state1 = WorldStateFactory.empty()
        state1.set("key1", true)
        state1.set("key2", true)

        val state2 = WorldStateFactory.empty()
        state2.set("key1", true)

        Assertions.assertTrue(state1.covers(state2))
        Assertions.assertFalse(state2.covers(state1))
    }

    @Test
    fun testEqualsAndHashCode() {
        val anotherStateMap = WorldStateFactory.empty()
        Assertions.assertEquals(state, anotherStateMap)
        Assertions.assertEquals(state.hashCode(), anotherStateMap.hashCode())

        state.set("key1", true)
        Assertions.assertNotEquals(state, anotherStateMap)
    }
}
