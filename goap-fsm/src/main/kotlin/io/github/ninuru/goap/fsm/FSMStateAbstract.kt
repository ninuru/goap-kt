package io.github.ninuru.goap.fsm

import io.github.ninuru.goap.time.TickTimer

abstract class FSMStateAbstract(protected val tickTimer: TickTimer = TickTimer()) : FSMState {

    override fun onInit() {
        resetDelay()
        _onInit()
    }

    protected abstract fun _onInit()

    override fun onTick(): FSMState? {
        if (!tickTimer.isReady()) {
            return this
        }
        return _onTick()
    }

    protected abstract fun _onTick(): FSMState?

    override fun onEnd() {}

    protected fun resetDelay() {
        tickTimer.reset()
    }

    protected fun delay() {
        delay(10, 25)
    }

    protected fun delay(ticks: Int) {
        tickTimer.wait(ticks)
    }

    protected fun delay(ticksMin: Int, ticksMax: Int) {
        tickTimer.waitRandom(ticksMin, ticksMax)
    }
}
