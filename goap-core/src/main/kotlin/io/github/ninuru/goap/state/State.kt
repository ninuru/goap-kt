package io.github.ninuru.goap.state

data class StateKey(
    val key: String,
    val group: String? = null
) {
    companion object {
        fun of(key: String, group: String? = null) = StateKey(key, group)
        fun groupOf(group: String) = StateKey("", group)
    }
}

data class StateBelief(
    val key: String,
    val value: Boolean = true,
    val group: String? = null
) {
    fun negate() = copy(value = !value)

    companion object {
        fun of(key: String, value: Boolean = true, group: String? = null) =
            StateBelief(key, value, group)

        fun groupOf(group: String) = StateBelief("", true, group)

        val NEVER = StateBelief("__never__", true, null)
    }
}
