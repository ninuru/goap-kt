package io.github.ninuru.goap.action

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.goal.Availability
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class ActionBuilder(private val name: String) {
    private var cost: Float = ActionCost.NORMAL.cost
    private var interruptible: Boolean = true
    private var preConditions = mutableSetOf<StateBelief>()
    private var runtimeConditions = mutableSetOf<StateBelief>()
    private var effects = mutableSetOf<StateBelief>()
    private var sideEffects = mutableSetOf<StateBelief>()
    private var costProvider: ActionCostProvider? = null
    private var completeProvider: ActionCompleteProvider? = null
    private var strategy: ActionStrategy? = null
    private var availability: Availability? = null
    private var onTick: ((Goap, BlackBoard, WorldState, Int) -> Status)? = null
    private var onInit: ((Goap, BlackBoard, WorldState) -> Unit)? = null
    private var onCleanup: ((Goap, BlackBoard, WorldState, Status, Int) -> Unit)? = null
    private var timeout: Int = 0

    fun cost(cost: Float) = apply { this.cost = cost }
    fun cost(cost: ActionCost) = apply { this.cost = cost.cost }
    fun withCostProvider(provider: ActionCostProvider) = apply { this.costProvider = provider }
    fun interruptible(value: Boolean) = apply { this.interruptible = value }
    fun addPreCondition(key: String, value: Boolean = true) = apply { preConditions.add(StateBelief.of(key, value)) }
    fun addPreCondition(belief: StateBelief) = apply { preConditions.add(belief) }
    fun addRuntimeCondition(belief: StateBelief) = apply { runtimeConditions.add(belief) }
    fun addEffect(key: String, value: Boolean = true) = apply { effects.add(StateBelief.of(key, value)) }
    fun addEffect(belief: StateBelief) = apply { effects.add(belief) }
    fun addSideEffect(belief: StateBelief) = apply { sideEffects.add(belief) }
    fun withComplete(provider: ActionCompleteProvider) = apply { this.completeProvider = provider }
    fun withStrategy(strategy: ActionStrategy) = apply { this.strategy = strategy }
    fun withAvailability(availability: Availability) = apply { this.availability = availability }
    fun onTick(block: (Goap, BlackBoard, WorldState, Int) -> Status) = apply { this.onTick = block }
    fun onInit(block: (Goap, BlackBoard, WorldState) -> Unit) = apply { this.onInit = block }
    fun onCleanup(block: (Goap, BlackBoard, WorldState, Status, Int) -> Unit) = apply { this.onCleanup = block }
    fun timeout(ticks: Int) = apply { this.timeout = ticks }

    fun build(): Action {
        val strategy = strategy ?: object : ActionStrategy {
            override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {
                onInit?.invoke(ai, bb, ws)
            }

            override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
                return onTick?.invoke(ai, bb, ws, ticksInAction) ?: Status.SUCCESS
            }

            override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {
                onCleanup?.invoke(ai, bb, ws, lastStatus, ticksInAction)
            }
        }

        return ActionBuilt(
            name = name,
            cost = cost,
            interruptible = interruptible,
            preConditions = preConditions.toSet(),
            runtimeConditions = runtimeConditions.toSet(),
            effects = effects.toSet(),
            sideEffects = sideEffects.toSet(),
            costProvider = costProvider,
            completeProvider = completeProvider,
            strategy = strategy,
            availability = availability,
            timeout = timeout
        )
    }
}

class ActionBuilt(
    override val name: String,
    override val cost: Float,
    private val interruptible: Boolean,
    override val preConditions: Set<StateBelief>,
    override val runtimeConditions: Set<StateBelief>,
    override val effects: Set<StateBelief>,
    override val sideEffects: Set<StateBelief>,
    private val costProvider: ActionCostProvider?,
    private val completeProvider: ActionCompleteProvider?,
    private val strategy: ActionStrategy,
    private val availability: Availability?,
    override val timeout: Int
) : Action {
    override val isInterruptible: Boolean = interruptible
    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return completeProvider?.isCompleted(ai, bb, ws) ?: true
    }
    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) = strategy.init(ai, bb, ws)
    override fun isPossible(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean = true
    override fun isAvailable(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return availability?.canBeActivated(ai, bb, ws) ?: true
    }
    override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) =
        strategy.cleanup(ai, bb, ws, lastStatus, ticksInAction)
    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status =
        strategy.tick(ai, bb, ws, ticksInAction)
}
