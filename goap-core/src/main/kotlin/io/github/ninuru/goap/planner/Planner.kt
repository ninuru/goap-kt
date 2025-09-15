package io.github.ninuru.goap.planner

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.action.Action
import io.github.ninuru.goap.goal.Goal

interface Planner {
    fun plan(ai: Goap): Plan?
    fun plan(ai: Goap, goals: List<Goal>): Plan?
    fun plan(ai: Goap, goal: Goal): Plan?
}
