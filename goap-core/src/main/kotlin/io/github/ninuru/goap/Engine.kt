package io.github.ninuru.goap

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.agent.Agent
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.blackboard.BlackBoardKey
import io.github.ninuru.goap.action.ActionConstraints
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.goal.Scenario
import io.github.ninuru.goap.logs.Logger
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.planner.Planner
import io.github.ninuru.goap.reports.Reporter
import io.github.ninuru.goap.state.StateProvider
import io.github.ninuru.goap.state.WorldState
import io.github.ninuru.goap.time.TickTimer

class Engine(
    private val _worldState: WorldState,
    private val _planner: Planner,
    private val _agent: Agent,
    private val _logger: Logger,
    private val _reporter: Reporter,
    private val timerFactory: () -> TickTimer = ::TickTimer
) : Goap {
    companion object {
        private const val MIN_TICKS_KEEP_PLAN = 20 * 2
        private const val MIN_TICKS_RETRY_PLAN = 20 * 2
        private const val MIN_TICKS_BETWEEN_PLANS = 10
    }

    private val toSleep = timerFactory()
    private val _blackBoard = BlackBoard()
    private val _goals = mutableListOf<Goal>()
    private val _actions = mutableListOf<Action>()
    private val stateProviders = mutableListOf<StateProvider>()
    private var activeScenario: Scenario = Scenario.NONE
    private var ticksWithoutPlan = Int.MAX_VALUE
    private var ticksLastCheckBetterPlan = 0

     override fun tick() {
         try {
             reporter.beginSection("Update State Providers").use {
                 for (stateProvider in this.stateProviders) {
                     stateProvider.updateState(this, blackBoard, worldState)
                 }
             }
         } catch (_: Exception) {
         }

        if (!this.toSleep.isReady()) {
            return
        }

        try {
            reporter.beginSection("Find Better Plan").use {
                if (_agent.hasPlanAndAction() && _agent.currentGoal!!.isInterruptible &&
                    _agent.currentAction!!.isInterruptible &&
                    _agent.ticksInCurrentPlan > MIN_TICKS_KEEP_PLAN &&
                    ticksLastCheckBetterPlan++ > MIN_TICKS_RETRY_PLAN
                ) {
                    ticksLastCheckBetterPlan = 0
                    val betterPlan = this.findBetterPlan(_agent.plan!!)
                    if (betterPlan != null) {
                        _agent.setPlan(this, betterPlan)
                    }
                }
            }
        } catch (_: Exception) {
        }

        try {
            reporter.beginSection("Find Initial Plan").use {
                if (!_agent.hasPlan() && ticksWithoutPlan++ > MIN_TICKS_RETRY_PLAN) {
                    this.ticksWithoutPlan = 0
                    this.planNextGoal()
                }
            }
        } catch (_: Exception) {
        }

        if (!_agent.hasPlan()) {
            return
        }

         this.ticksWithoutPlan = MIN_TICKS_RETRY_PLAN - MIN_TICKS_BETWEEN_PLANS
         try {
             reporter.beginSection("Agent Tick").use {
                 _agent.tick(this, blackBoard, worldState)
             }
         } catch (_: Exception) {
         }
     }

    private fun planNextGoal() {
         val lastGoal = _agent.lastGoal
         if (lastGoal != null && !lastGoal.isInterruptible && !lastGoal.isCompleted(this, blackBoard, worldState)) {
             reporter.pushDebug("Planning for previous plan")
             _agent.setPlan(this, _planner.plan(this, lastGoal))
         } else {
             reporter.pushDebug("Planning for new plan")
             _agent.setPlan(this, _planner.plan(this, this.getGoals()))
         }
     }

     override fun reset() {
         try {
             reporter.beginSection("AI Reset").use {
                 _agent.reset(this, blackBoard, worldState)
                 this.ticksWithoutPlan = Int.MAX_VALUE
                 this.ticksLastCheckBetterPlan = 0
                 _worldState.clear()
             }
        } catch (_: Exception) {
        }
    }

    override fun sleep(ticks: Int) {
        this.toSleep.wait(ticks)
    }

    private fun findBetterPlan(currentPlan: Plan): Plan? {
        val newGoals = this.getGoals(currentPlan.goal.getRelevance(this, blackBoard, worldState))
        if (!newGoals.isEmpty()) {
            val newPlan = _planner.plan(this, newGoals)
            if (newPlan == null || newPlan.goal == currentPlan.goal &&
                currentPlan.totalCost <= newPlan.totalCost
            ) {
                return null
            }
            return newPlan
        }
        return null
    }

    override val worldState: WorldState get() = _worldState

    override val planner: Planner get() = _planner

    override val agent: Agent get() = _agent

    override val blackBoard: BlackBoard get() = _blackBoard

    override val logger: Logger get() = _logger

    override val reporter: Reporter get() = _reporter

    override fun getGoals(): List<Goal> = getGoals(0)

    override fun getGoals(minRelevance: Int): List<Goal> {
        return (_goals + this.activeScenario.goals)
            .filter { it.getRelevance(this, blackBoard, worldState) >= minRelevance }
            .filter { !it.isCompleted(this, blackBoard, worldState) }
            .filter { it.canBeActivated(this, blackBoard, worldState) }
            .sortedByDescending { it.getRelevance(this, blackBoard, worldState) }
    }

    override val actions: List<Action> get() = _actions

    override val availableActions: List<Action> get() =
        _actions.filter { it.isAvailable(this, blackBoard, worldState) }

    override val scenario: Scenario get() = activeScenario

    override fun setScenario(scenario: Scenario) {
        this.activeScenario = scenario
    }

    override fun addGoal(goal: Goal) {
        _goals.add(goal)
    }

    override fun addAction(action: Action) {
         ActionConstraints.validateAction(action)
         _actions.add(action)
     }

    override fun addActions(vararg actions: Action) {
        for (action in actions) {
            addAction(action)
        }
    }

    override fun addStateProvider(stateProvider: StateProvider) {
        this.stateProviders.add(stateProvider)
    }
}

