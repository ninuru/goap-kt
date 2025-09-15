package io.github.ninuru.goap.test.planner

import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.algorithms.GoalActionFilterImpl
import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class GoalActionFilterTest {
    private lateinit var filter: GoalActionFilterImpl
    private lateinit var current: WorldState
    private lateinit var goal: WorldState
    private lateinit var action1: Action
    private lateinit var action2: Action
    private lateinit var action3: Action

    @BeforeEach
    fun setUp() {
        filter = GoalActionFilterImpl()
        current = WorldStateFactory.empty()
        goal = WorldStateFactory.empty()
        action1 = Action.builder("Action1").addEffect("teo_1", true).build()
        action2 = Action.builder("Action2").addEffect("teo_2", true).build()
        action3 = Action.builder("Action3").addEffect("teo_3", true).build()
    }

    @Test
    fun testFilterWhenCurrentCoversGoal() {
        current.set("teo_1")
        goal.set("teo_1")

        val result = filter.applyFilter(current, goal, listOf(action1, action2, action3))

        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterWhenNoActionsCanAchieveGoal() {
        current.set("idk")
        goal.set("non_reachable")

        assertThrows(IOException::class.java) {
            filter.applyFilter(current, goal, emptyList())
        }
    }

    @Test
    fun testFilterWhenActionsCanAchieveGoal() {
        goal.set("teo_1")
        goal.set("teo_2")

        val result = filter.applyFilter(current, goal, listOf(action1, action2, action3))

        assertEquals(2, result.size)
        assertTrue(result.contains(action1))
        assertTrue(result.contains(action2))
    }
}
