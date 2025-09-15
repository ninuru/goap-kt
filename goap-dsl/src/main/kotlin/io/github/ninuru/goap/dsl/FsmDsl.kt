package io.github.ninuru.goap.dsl

import io.github.ninuru.goap.action.ActionStrategy
import io.github.ninuru.goap.fsm.FSM
import io.github.ninuru.goap.fsm.ErrorHandler
import io.github.ninuru.goap.fsm.FSMAbstract
import io.github.ninuru.goap.fsm.FSMState
import io.github.ninuru.goap.fsm.FSMStateAbstract
import io.github.ninuru.goap.fsm.ActionStrategyFSM
import io.github.ninuru.goap.time.TickTimer

/**
 * Creates an FSM (finite state machine).
 *
 * Example:
 * ```
 * val myFsm = fsm {
 *     initialState(fsmState {
 *         onInit { println("Started") }
 *         onTick { FSMState.END }
 *     })
 * }
 * ```
 */
fun fsm(configure: FsmScope.() -> Unit): FSM {
    val scope = FsmScope()
    scope.configure()
    return scope.build()
}

/**
 * Creates an action strategy from an FSM.
 *
 * Example:
 * ```
 * action("my_action") {
 *     strategy(fsmStrategy {
 *         initialState(fsmState { ... })
 *     })
 * }
 * ```
 */
fun fsmStrategy(configure: FsmScope.() -> Unit): ActionStrategy {
    val fsm = fsm(configure)
    return ActionStrategyFSM(fsm)
}

/**
 * Creates an FSM state inline.
 *
 * Example:
 * ```
 * fsmState {
 *     onInit { println("Initialized") }
 *     onTick { FSMState.END }
 * }
 * ```
 */
fun fsmState(configure: FsmStateScope.() -> Unit): FSMState {
    val scope = FsmStateScope()
    scope.configure()
    return scope.build()
}

/**
 * DSL receiver for building an FSM.
 */
@GoapDsl
class FsmScope {
    var errorHandler: ErrorHandler? = null
    var minDelay: Int = 0
    var maxDelay: Int = 0

    private var initialState: FSMState? = null

    /**
     * Sets the initial FSM state.
     */
    fun initialState(state: FSMState) {
        this.initialState = state
    }

    /**
     * Defines the initial FSM state inline.
     */
    fun initialState(configure: FsmStateScope.() -> Unit) {
        val scope = FsmStateScope()
        scope.configure()
        this.initialState = scope.build()
    }

    internal fun build(): FSM {
        val initialState = this.initialState ?: throw IllegalStateException("FSM must have an initial state")
        val minDelay = this.minDelay
        val maxDelay = this.maxDelay

        return object : FSMAbstract(errorHandler) {
            override fun getInitialState(): FSMState = initialState
            override fun getMinDelay(): Int = minDelay
            override fun getMaxDelay(): Int = maxDelay
        }
    }
}

/**
 * DSL receiver for defining an FSM state.
 */
@GoapDsl
class FsmStateScope {
    private var onInitBlock: (() -> Unit)? = null
    private var onTickBlock: (() -> FSMState?)? = null
    private var onEndBlock: (() -> Unit)? = null
    private var tickTimer: TickTimer = TickTimer()

    /**
     * Sets the init callback.
     */
    fun onInit(block: () -> Unit) {
        this.onInitBlock = block
    }

    /**
     * Sets the tick callback (returns next state or null to end).
     */
    fun onTick(block: () -> FSMState?) {
        this.onTickBlock = block
    }

    /**
     * Sets the end callback.
     */
    fun onEnd(block: () -> Unit) {
        this.onEndBlock = block
    }

    internal fun build(): FSMState {
        val onInit = this.onInitBlock
        val onTick = this.onTickBlock
        val onEnd = this.onEndBlock

        return object : FSMStateAbstract(tickTimer) {
            override fun _onInit() {
                onInit?.invoke()
            }

            override fun _onTick(): FSMState? {
                return onTick?.invoke()
            }

            override fun onEnd() {
                onEnd?.invoke()
            }
        }
    }
}
