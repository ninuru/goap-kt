package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScenarioDslTest {

    @Test
    fun testScenarioWithInlineGoals() {
        val scenario = scenario {
            goal("goal1") { relevance = 1 }
            goal("goal2") { relevance = 2 }
            goal("goal3") { relevance = 3 }
        }

        assertEquals(3, scenario.goals.size)
        assertTrue(scenario.goals.any { it.name == "goal1" })
        assertTrue(scenario.goals.any { it.name == "goal2" })
        assertTrue(scenario.goals.any { it.name == "goal3" })
    }

    @Test
    fun testScenarioWithClassBasedGoals() {
        val customGoal = object : Goal {
            override val name = "custom"
            override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState) = false
            override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
            override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
        }

        val scenario = scenario {
            goal(customGoal)
        }

        assertEquals(1, scenario.goals.size)
        assertEquals("custom", scenario.goals[0].name)
    }

    @Test
    fun testScenarioMixed() {
        val customGoal = object : Goal {
            override val name = "pre_built"
            override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState) = false
            override fun activate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
            override fun deactivate(ai: Goap, bb: BlackBoard, ws: WorldState) {}
        }

        val scenario = scenario {
            goal("inline1") { relevance = 1 }
            goal(customGoal)
            goal("inline2") { relevance = 2 }
        }

        assertEquals(3, scenario.goals.size)
    }

    @Test
    fun testSetScenarioOnAI() {
        val testScenario = scenario {
            goal("scenario_goal") { relevance = 5 }
        }

        val ai = goap {
            scenario(testScenario)
        }

        assertEquals(1, ai.scenario.goals.size)
        assertEquals("scenario_goal", ai.scenario.goals[0].name)
    }

    @Test
    fun testEmptyScenario() {
        val emptyScenario = scenario {
        }

        assertEquals(0, emptyScenario.goals.size)
    }

     @Test
     fun testScenarioGoalConditions() {
         val mockAI = goap { }
        
        val scenario = scenario {
            goal("farming") {
                conditions {
                    "in_garden" to true
                    "has_seeds" to true
                }
                relevance = 3
            }
        }

        val goal = scenario.goals[0]
        assertEquals(2, goal.conditions.size)
        assertEquals(3, goal.getRelevance(mockAI, mockAI.blackBoard, mockAI.worldState))
    }

    @Test
    fun testMultipleScenarios() {
        val scenario1 = scenario {
            goal("s1_goal") { relevance = 1 }
        }

        val scenario2 = scenario {
            goal("s2_goal") { relevance = 2 }
        }

        assertEquals(1, scenario1.goals.size)
        assertEquals(1, scenario2.goals.size)
        assertNotEquals(scenario1.goals[0].name, scenario2.goals[0].name)
    }
}
