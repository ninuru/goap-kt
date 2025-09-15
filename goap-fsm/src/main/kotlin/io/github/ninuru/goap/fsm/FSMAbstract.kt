package io.github.ninuru.goap.fsm

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.WorldState
import kotlin.random.Random

abstract class FSMAbstract(private val errorHandler: ErrorHandler? = null) : FSM {
    protected val random = Random
    protected var currentState: FSMState? = FSMState.forward(getInitialState())
    protected var delay = 0

    override fun activate() {}

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState): Status {
        if (--delay > 0) {
            return Status.RUNNING
        }
        delay = 0

        if (currentState == null) {
            return Status.FAILED
        }

        return try {
            val nextState = currentState!!.onTick()

            if (nextState != currentState) {
                ai.reporter.pushDebug("Transitioning to state: ${nextState?.javaClass?.simpleName ?: "End"}")
                currentState!!.onEnd()
                delay = getRandomDelay()
                if (nextState != null) nextState.onInit()
            }

            currentState = nextState

            if (currentState == null) {
                Status.SUCCESS
            } else {
                Status.RUNNING
            }
        } catch (ex: Exception) {
            errorHandler?.handleError(ex)
            Status.FAILED
        }
    }

    fun getRandomDelay(): Int {
        var min = getMinDelay()
        val max = getMaxDelay()
        if (min > max) {
            min = max
        }

        val p = (random.nextInt(1000) + 1) / 1000.0
        val q = 1.0 - Math.sqrt(p)
        return (min + q * (max - min)).toInt()
    }

    protected open fun getMaxDelay(): Int = 0
    protected open fun getMinDelay(): Int = 0

    protected abstract fun getInitialState(): FSMState

    override fun deactivate() {
        currentState?.onEnd()
        currentState = FSMState.forward(getInitialState())
    }

    override fun allowInventory(): Boolean = false
}
