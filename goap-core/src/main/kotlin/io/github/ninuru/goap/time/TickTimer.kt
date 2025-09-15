package io.github.ninuru.goap.time

import kotlin.math.sqrt
import kotlin.random.Random

class TickTimer {
    private val random = Random.Default
    private var current = 0
    private var toWait = 0

    fun wait(ticks: Int) {
        current = 0
        toWait = ticks
    }

    fun waitRandom(minTicks: Int, maxTicks: Int) {
        current = 0
        toWait = if (minTicks == maxTicks) {
            maxTicks
        } else {
            random.nextInt(maxTicks - minTicks) + minTicks
        }
    }

    fun isReady(addTick: Boolean = true): Boolean {
        if (addTick) addTick()
        return current >= toWait
    }

    fun addTick(amount: Int = 1) {
        current += amount
        current = minOf(current, Int.MAX_VALUE - 20)
    }

    fun reset() {
        current = 0
        toWait = 0
    }
}
