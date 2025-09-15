package io.github.ninuru.goap.blackboard

data class BlackBoardKey<T>(val key: String)

class BlackBoard {
    private val storage = mutableMapOf<String, Any?>()

    fun <T : Any> put(key: BlackBoardKey<T>, value: T) {
        storage[key.key] = value
    }

    fun <T : Any> get(key: BlackBoardKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return storage[key.key] as T?
    }

    fun <T : Any> getOrDefault(key: BlackBoardKey<T>, default: T): T {
        return get(key) ?: default
    }

    fun clear() {
        storage.clear()
    }
}
