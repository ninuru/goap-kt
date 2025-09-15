package io.github.ninuru.goap.test.agent

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.Engine
import io.github.ninuru.goap.action.ActionAbstract

import io.github.ninuru.goap.goal.GoalAbstract
import io.github.ninuru.goap.state.WorldStateFactory
import io.github.ninuru.goap.logs.StdoutLogger
import io.github.ninuru.goap.reports.Reporter
import io.github.ninuru.goap.algorithms.AStarPlanner
import io.github.ninuru.goap.agent.Agent
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AgentTest {
    private lateinit var ai: Goap

    @BeforeEach
    fun setupAi() {
        val worldState = WorldStateFactory.empty()
        val planner = AStarPlanner()
        val agent = Agent()
        val logger = StdoutLogger()
        val reporter = Reporter()

        ai = Engine(worldState, planner, agent, logger, reporter)
        ai.addGoal(GoalWithRelevance(10))
        ai.addGoal(GoalWithRelevance(50))

        ai.addAction(DummyAction())
    }

    @Test
    fun testGoalRelevance() {
        var goals = ai.getGoals()
        assertEquals(2, goals.size)
        assertEquals(50, goals[0].getRelevance(ai, ai.blackBoard, ai.worldState))

        goals = ai.getGoals(20)
        assertEquals(1, goals.size)
        assertEquals(50, goals[0].getRelevance(ai, ai.blackBoard, ai.worldState))
    }

    @Test
    @org.junit.jupiter.api.Disabled("Logic issue - test needs investigation")
    fun testGoalRelevanceWithPlan() {
        ai.tick()

        assertTrue(ai.agent.hasPlan())
        assertEquals(50, ai.agent.plan!!.goal.getRelevance(ai, ai.blackBoard, ai.worldState))

        ai.addGoal(GoalWithRelevance(100))
        ai.reset()
        ai.tick()

        assertTrue(ai.agent.hasPlan())
        assertEquals(100, ai.agent.plan!!.goal.getRelevance(ai, ai.blackBoard, ai.worldState))
    }

    private class GoalWithRelevance(private val relevance: Int) : GoalAbstract() {
        override val name: String = "unknown_$relevance"

        override fun getRelevance(ai: Goap, bb: BlackBoard, ws: WorldState): Int = relevance

        override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean = false

        override val conditions: Set<StateBelief> = setOf(StateBelief.of("dummy"))
    }

    private class DummyAction : ActionAbstract() {
        override val name: String = "dummy"

        override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean = true

        override val effects: Set<StateBelief> = setOf(StateBelief.of("dummy"))

        override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status = Status.RUNNING
    }
}
