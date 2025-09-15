package io.github.ninuru.goap.agent

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Goal
import io.github.ninuru.goap.planner.Plan
import io.github.ninuru.goap.state.WorldState
import io.github.ninuru.goap.time.TickTimer

enum class AdvanceReason {
    INITIAL_ACTION,
    ACTION_EARLY_COMPLETE,
    ACTION_SUCCESS,
    ACTION_FAIL,
    ACTION_TIMEOUT,
    ACTION_CONDITIONS_NOT_NET,
    ACTION_UNAVAILABLE,
    ACTION_SUCCESS_BUT_NOT_EFFECTIVE,
    GOAL_COMPLETED,
    NEW_PLAN,
    RESET
}

class Agent(
    private val timerFactory: () -> TickTimer = ::TickTimer
) {
    companion object {
        private const val MIN_TICKS_WAIT_BETWEEN_ACTIONS = 2
        private const val MAX_TICKS_WAIT_BETWEEN_ACTIONS = 4
    }

    private val waiter = timerFactory()
    private var _lastGoal: Goal? = null
    private var _plan: Plan? = null
    private var _currentAction: Action? = null
    private var _ticksInCurrentPlan = 0
    private var _ticksInCurrentAction = 0
    private var lastActionStatus = Status.RUNNING

    fun tick(ai: Goap, bb: BlackBoard, ws: WorldState): Status {
        if (!hasPlan()) return Status.FAILED
        if (!waiter.isReady()) return Status.RUNNING
        _ticksInCurrentPlan++

        if (_currentAction == null) this.advancePlan(ai, bb, ws, AdvanceReason.INITIAL_ACTION)
        if (_currentAction == null) return Status.FAILED

        if (isActionInterruptible() && this._plan?.goal?.isCompleted(ai, bb, ws) == true) {
            this.discardPlan(ai, bb, ws, AdvanceReason.GOAL_COMPLETED)
            return Status.SUCCESS
        }

        if (!_currentAction!!.isAvailable(ai, bb, ws)) {
            this.discardPlan(ai, bb, ws, AdvanceReason.ACTION_UNAVAILABLE)
            return Status.FAILED
        }

        if (isActionInterruptible() && _currentAction!!.isCompleted(ai, bb, ws)) {
            return this.advancePlan(ai, bb, ws, AdvanceReason.ACTION_EARLY_COMPLETE)
        }

        if (!_currentAction!!.isPossible(ai, bb, ws)) {
            this.discardPlan(ai, bb, ws, AdvanceReason.ACTION_CONDITIONS_NOT_NET)
            return Status.FAILED
        }

        if (_ticksInCurrentAction++ == 0) {
            ai.reporter.pushDebug("Action Init: %s", _currentAction!!)
            _currentAction!!.init(ai, bb, ws)
        }

        try {
            lastActionStatus = _currentAction!!.tick(ai, bb, ws, _ticksInCurrentAction)
            when (lastActionStatus) {
                Status.RUNNING -> {
                    if (_currentAction!!.timeout != 0 && _ticksInCurrentAction > _currentAction!!.timeout) {
                        this.discardPlan(ai, bb, ws, AdvanceReason.ACTION_TIMEOUT)
                        return Status.FAILED
                    }
                    return Status.RUNNING
                }
                Status.SUCCESS -> {
                    if (!this.confirmSuccess(ai, bb, ws)) {
                        ai.reporter.pushError("Promised Effects of %s, was not successful", this._currentAction!!.name)
                        for (effect in this._currentAction!!.effects) {
                            if (!ws.match(effect)) {
                                ai.reporter.pushError("- %s does not match", effect.key)
                            }
                        }
                        discardPlan(ai, bb, ws, AdvanceReason.ACTION_SUCCESS_BUT_NOT_EFFECTIVE)
                        return Status.FAILED
                    }
                    return this.advancePlan(ai, bb, ws, AdvanceReason.ACTION_SUCCESS)
                }
                Status.FAILED -> {
                    this.discardPlan(ai, bb, ws, AdvanceReason.ACTION_FAIL)
                    return Status.FAILED
                }
            }
        } catch (e: Exception) {
            return Status.FAILED
        }

        return Status.RUNNING
    }

    private fun confirmSuccess(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        if (_currentAction == null) return true
        return this._currentAction!!.isCompleted(ai, bb, ws)
    }

    private fun isActionInterruptible(): Boolean {
        return _ticksInCurrentAction == 0 || _currentAction!!.isInterruptible
    }

    private fun discardPlan(ai: Goap, bb: BlackBoard, ws: WorldState, reason: AdvanceReason) {
        if (_currentAction != null) {
            ai.reporter.pushDebug("Cleaning up previous Action due to %s", reason)
            _currentAction!!.cleanup(ai, bb, ws, lastActionStatus, _ticksInCurrentAction)
            _currentAction = null
            lastActionStatus = Status.RUNNING
        }
        if (_plan != null) {
            ai.reporter.pushDebug("Discarding Plan due to %s", reason)
            _lastGoal = _plan!!.goal
            _plan!!.goal.deactivate(ai, bb, ws)
            _plan = null
        }
    }

    private fun advancePlan(ai: Goap, bb: BlackBoard, ws: WorldState, reason: AdvanceReason): Status {
        if (_currentAction != null) {
            ai.reporter.pushDebug("Cleaning up previous Action due to %s", reason)
            _currentAction!!.cleanup(ai, bb, ws, lastActionStatus, _ticksInCurrentAction)
        }
        if (!this.hasPlan()) {
            return Status.FAILED
        }
        ai.reporter.pushDebug("Advancing Plan due to %s", reason)
        _ticksInCurrentAction = 0
        _currentAction = _plan!!.nextAction()
        if (_currentAction == null) {
            discardPlan(ai, bb, ws, reason)
            return Status.SUCCESS
        }
        waiter.waitRandom(MIN_TICKS_WAIT_BETWEEN_ACTIONS, MAX_TICKS_WAIT_BETWEEN_ACTIONS)
        ai.reporter.pushDebug("Continuing with action : %s", _currentAction!!)
        return Status.RUNNING
    }

    fun setPlan(ai: Goap, plan: Plan?) {
        _ticksInCurrentPlan = 0
        this.discardPlan(ai, ai.blackBoard, ai.worldState, AdvanceReason.NEW_PLAN)
        if (plan != null) {
            ai.reporter.pushDebug("Setting plan to : %s", plan)
            this._plan = plan
            this._plan!!.goal.activate(ai, ai.blackBoard, ai.worldState)
        }
    }

    val plan: Plan? get() = _plan

    val lastGoal: Goal? get() = _lastGoal

    val currentGoal: Goal? get() = if (hasPlan()) _plan!!.goal else null

    val currentAction: Action? get() = _currentAction

    fun hasPlan(): Boolean = _plan != null && _plan!!.goal != null

    fun hasPlanAndAction(): Boolean = hasPlan() && this._currentAction != null

    val ticksInCurrentPlan: Int get() = _ticksInCurrentPlan

    val ticksInCurrentAction: Int get() = _ticksInCurrentAction

    fun reset(ai: Goap, bb: BlackBoard, ws: WorldState) {
        this.discardPlan(ai, bb, ws, AdvanceReason.RESET)
        this.lastActionStatus = Status.RUNNING
        this.waiter.reset()
        this._lastGoal = null
        this._ticksInCurrentPlan = 0
        this._ticksInCurrentAction = 0
    }
}
