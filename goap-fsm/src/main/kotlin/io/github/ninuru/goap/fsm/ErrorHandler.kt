package io.github.ninuru.goap.fsm

fun interface ErrorHandler {
    fun handleError(exception: Exception)
}
