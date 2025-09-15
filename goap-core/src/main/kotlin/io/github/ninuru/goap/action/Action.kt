package io.github.ninuru.goap.action

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

enum class ActionCost(val cost: Float) {
    NONE(0.5f),
    VERY_LOW(2f),
    LOW(3.5f),
    NORMAL(5f),
    HIGH(6.5f),
    VERY_HIGH(8f),
    EXPENSIVE(10f)
}

fun interface ActionCostProvider {
    fun getCost(ai: Goap, bb: BlackBoard, ws: WorldState): Float
}

fun interface ActionCompleteProvider {
    fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean
}

interface ActionStrategy {
    fun init(ai: Goap, bb: BlackBoard, ws: WorldState)
    fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status
    fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int)
    fun allowInventory(): Boolean = false

    companion object {
        fun compound(): ActionStrategyCompoundBuilder = ActionStrategyCompoundBuilder()
    }
}

class ActionStrategyCompoundBuilder {
    private val strategies = mutableListOf<Pair<String, ActionStrategy>>()

    fun then(name: String, strategy: ActionStrategy) = apply {
        strategies.add(name to strategy)
    }

    fun build(): ActionStrategy = ActionStrategyCompound(strategies)
}

interface Action {
    val name: String
    val cost: Float get() = ActionCost.NORMAL.cost
    val isInterruptible: Boolean get() = true
    val preConditions: Set<StateBelief> get() = emptySet()
    val runtimeConditions: Set<StateBelief> get() = emptySet()
    val sideEffects: Set<StateBelief> get() = emptySet()
    val effects: Set<StateBelief> get() = emptySet()
    fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean
    fun init(ai: Goap, bb: BlackBoard, ws: WorldState)
    fun isPossible(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean
    fun isAvailable(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean = true
    fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int)
    fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status
    val timeout: Int get() = 0

    companion object {
        fun builder(name: String): ActionBuilder = ActionBuilder(name)
    }
}

class ActionSorter : Comparator<Action> {
    override fun compare(a: Action, b: Action): Int = a.cost.compareTo(b.cost)
}

internal class ActionStrategyCompound(
    private val strategies: List<Pair<String, ActionStrategy>>
) : ActionStrategy {
    private var currentIndex = 0

    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {
        currentIndex = 0
        if (strategies.isNotEmpty()) {
            strategies[0].second.init(ai, bb, ws)
        }
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        if (currentIndex >= strategies.size) return Status.SUCCESS
        val (_, strategy) = strategies[currentIndex]
        val status = strategy.tick(ai, bb, ws, ticksInAction)
        if (status == Status.SUCCESS && currentIndex + 1 < strategies.size) {
            currentIndex++
            strategies[currentIndex].second.init(ai, bb, ws)
            return Status.RUNNING
        }
        return status
    }

    override fun cleanup(ai: Goap, bb: BlackBoard, ws: WorldState, lastStatus: Status, ticksInAction: Int) {
        if (currentIndex < strategies.size) {
            strategies[currentIndex].second.cleanup(ai, bb, ws, lastStatus, ticksInAction)
        }
    }

    override fun allowInventory(): Boolean = false
}
