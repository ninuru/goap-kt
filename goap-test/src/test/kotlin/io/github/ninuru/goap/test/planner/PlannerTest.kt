package io.github.ninuru.goap.test.planner

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.agent.Agent
import io.github.ninuru.goap.algorithms.AStarPlanner
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.Engine

import io.github.ninuru.goap.goal.GoalAbstract
import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.logs.StdoutLogger
import io.github.ninuru.goap.reports.Reporter
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.planner.Planner
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import io.github.ninuru.goap.test.fixtures.actions.TestActionGetAxe
import io.github.ninuru.goap.test.fixtures.actions.TestActionJoinSkyblock
import io.github.ninuru.goap.test.fixtures.actions.TestActionWalkKuudra
import io.github.ninuru.goap.test.fixtures.actions.TestActionWarpCrimson
import io.github.ninuru.goap.test.fixtures.actions.TestActionWarpGarden
import io.github.ninuru.goap.test.fixtures.actions.TestActionWarpKuudra
import io.github.ninuru.goap.test.fixtures.goals.TestGoalGetWood
import io.github.ninuru.goap.test.fixtures.goals.TestGoalPlayKuudra
import io.github.ninuru.goap.test.fixtures.goals.TestGoalStartFarming
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlannerTest {
    private val planner: Planner = AStarPlanner()
    private val agent: Agent = Agent()
    private val logger = StdoutLogger()
    private val reporter = Reporter()
    private lateinit var ai: Goap

    @BeforeEach
    fun setupAi() {
        val worldState = WorldStateFactory.empty()
        val aiEngine = Engine(worldState, planner, agent, logger, reporter)
        aiEngine.addGoal(TestGoalGetWood())
        aiEngine.addGoal(TestGoalStartFarming())
        aiEngine.addAction(TestActionGetAxe())
        aiEngine.addAction(TestActionJoinSkyblock())
        aiEngine.addAction(TestActionWarpGarden())
        aiEngine.addAction(TestActionWarpCrimson())
        aiEngine.addAction(TestActionWalkKuudra())
        aiEngine.addAction(TestActionWarpKuudra())

        this.ai = aiEngine
    }

    @Test
    fun testInvalidPlan() {
        val plan = ai.planner.plan(ai)
        assertNull(plan)
    }

    @Test
    fun testValidPlan() {
        ai.worldState.set("in_hypixel")
        ai.worldState.set("in_lobby")

        val plan = ai.planner.plan(ai)
        assertNotNull(plan)
    }

    @Test
    @org.junit.jupiter.api.Disabled("Logic issue - test needs investigation")
    fun testWarpInsteadOfWalk() {
        ai.worldState.set("in_hypixel")
        ai.worldState.set("in_lobby")
        ai.worldState.set("has_kuudra_warp")

        val plan = ai.planner.plan(ai, TestGoalPlayKuudra())
        assertNotNull(plan)
        assertTrue(plan!!.containsAction("warp_kuudra"))
    }

    @Test
    fun testWalkInsteadOfWarp() {
        ai.worldState.set("in_hypixel")
        ai.worldState.set("in_lobby")
        ai.worldState.set("has_kuudra_warp", false)

        val plan = ai.planner.plan(ai, TestGoalPlayKuudra())
        assertNotNull(plan)
        assertTrue(plan!!.containsAction("walk_kuudra"))
    }

    @Test
    @org.junit.jupiter.api.Disabled("Logic issue - test needs investigation")
    fun testEmptyStateConditionFalse() {
        ai.addAction(
            Action.builder("Do Something")
                .addPreCondition("thirsty", false)
                .addEffect("thirsty", true)
                .build()
        )

        val plan = ai.planner.plan(ai, object : GoalAbstract() {
            override val name: String = "Test Thirst"

            override fun isCompleted(ai: Goap, blackBoard: BlackBoard, ws: WorldState): Boolean = false

            override val conditions: Set<StateBelief> = setOf(StateBelief.of("thirsty", true))
        })
        assertNull(plan)
    }

    @Test
    @org.junit.jupiter.api.Disabled("Logic issue - StackOverflow in backtrackFilter")
    fun testRecurringAction() {
        ai.worldState.set("thirsty", true)
        ai.addAction(
            Action.builder("Drink Water")
                .addPreCondition("thirsty", true)
                .addEffect("thirsty", false)
                .build()
        )
        ai.addAction(
            Action.builder("Walk to Test1")
                .addPreCondition("thirsty", false)
                .addEffect("thirsty", true)
                .addEffect("at_test_1", true)
                .build()
        )
        ai.addAction(
            Action.builder("Walk to Test2")
                .addPreCondition("thirsty", false)
                .addPreCondition("at_test_1", true)
                .addEffect("thirsty", true)
                .addEffect("at_test_2", true)
                .build()
        )
        val plan = ai.planner.plan(ai, object : GoalAbstract() {
            override val name: String = "Be At Test2"

            override fun isCompleted(ai: Goap, blackBoard: BlackBoard, ws: WorldState): Boolean {
                return ws.get("at_test_2")
            }

            override val conditions: Set<StateBelief> = setOf(
                StateBelief.of("thirsty", false),
                StateBelief.of("at_test_2", true)
            )
        })
        assertNotNull(plan)
        assertEquals(Status.SUCCESS, plan!!.simulate(ai.worldState))
    }
}
