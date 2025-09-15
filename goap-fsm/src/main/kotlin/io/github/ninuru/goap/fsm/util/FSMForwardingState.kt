package io.github.ninuru.goap.fsm.util

import io.github.ninuru.goap.fsm.FSMState
import io.github.ninuru.goap.time.TickTimer

class FSMForwardingState(
    protected val next: FSMState,
    protected val tickTimer: TickTimer = TickTimer()
) : FSMState {

    constructor(next: FSMState, delayTicks: Int) : this(
        next,
        TickTimer().apply { wait(delayTicks) }
    )

    override fun onInit() {}

    override fun onTick(): FSMState? {
        return if (tickTimer.isReady()) next else this
    }

    override fun onEnd() {}
}
