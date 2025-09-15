package io.github.ninuru.goap.fsm

import io.github.ninuru.goap.fsm.util.FSMForwardingState

interface FSMState {
    fun onInit()
    fun onTick(): FSMState?
    fun onEnd()

    companion object {
        val END: FSMState? = null

        fun forward(next: FSMState): FSMForwardingState {
            return FSMForwardingState(next)
        }

        fun forward(next: FSMState, delayTicks: Int): FSMForwardingState {
            return FSMForwardingState(next, delayTicks)
        }
    }
}
