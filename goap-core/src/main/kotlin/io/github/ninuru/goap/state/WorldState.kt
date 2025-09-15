package io.github.ninuru.goap.state

class WorldState {
    private val state = mutableMapOf<String, Boolean>()

    fun get(key: String): Boolean = state[key] ?: false

    fun set(key: String, value: Boolean = true) {
        state[key] = value
    }

    fun set(key: StateKey, value: Boolean = true) {
        state[key.key] = value
    }

    fun set(belief: StateBelief) {
        state[belief.key] = belief.value
    }

    fun setAll(other: WorldState) {
        state.putAll(other.props)
    }

    fun clear() {
        state.clear()
    }

    fun clone(): WorldState {
        val copy = WorldState()
        copy.setAll(this)
        return copy
    }

    fun covers(other: WorldState): Boolean {
        return other.props.all { (key, value) ->
            get(key) == value
        }
    }

    fun match(belief: StateBelief): Boolean {
        return get(belief.key) == belief.value
    }

    val size: Int
        get() = state.size

    val props: Map<String, Boolean>
        get() = state.toMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorldState) return false
        return state == other.state
    }

    override fun hashCode(): Int = state.hashCode()

    override fun toString(): String = state.toString()
}

object WorldStateFactory {
    fun empty(): WorldState = WorldState()

    fun fromEffects(effects: Set<StateBelief>): WorldState {
        val state = empty()
        effects.forEach { state.set(it) }
        return state
    }

    fun fromConditions(conditions: Set<StateBelief>): WorldState {
        val state = empty()
        conditions.forEach { state.set(it) }
        return state
    }

    fun fromBeliefs(beliefs: Set<StateBelief>): WorldState {
        val state = empty()
        beliefs.forEach { state.set(it) }
        return state
    }

    fun merge(state1: WorldState, state2: WorldState): WorldState {
        val merged = empty()
        merged.setAll(state1)
        merged.setAll(state2)
        return merged
    }

    fun merge(state1: WorldState, state2: Set<StateBelief>, state3: Set<StateBelief>): WorldState {
        val merged = empty()
        merged.setAll(state1)
        state2.forEach { merged.set(it) }
        state3.forEach { merged.set(it) }
        return merged
    }

    fun delta(start: WorldState, goal: WorldState): Int {
        var count = 0
        for ((key, goalValue) in goal.props) {
            val startValue = start.get(key)
            if (startValue != goalValue) {
                count++
            }
        }
        return count
    }

    fun difference(start: WorldState, goal: WorldState): WorldState {
        val diff = empty()
        for ((key, goalValue) in goal.props) {
            val startValue = start.get(key)
            if (startValue != goalValue) {
                diff.set(key, goalValue)
            }
        }
        return diff
    }
}
