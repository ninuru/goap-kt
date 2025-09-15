package io.github.ninuru.goap.test.fixtures.actions

import io.github.ninuru.goap.Goap
import io.github.ninuru.goap.Status
import io.github.ninuru.goap.blackboard.BlackBoard
import io.github.ninuru.goap.action.ActionAbstract as ActionAbstract
import io.github.ninuru.goap.state.StateBelief
import io.github.ninuru.goap.state.WorldState

class TestActionGetAxe : ActionAbstract() {
    override val name: String = "get_axe"

    override val preConditions: Set<StateBelief> = setOf(StateBelief.of("has_axe_in_inventory"))

    override val effects: Set<StateBelief> = setOf(StateBelief.of("has_axe"))

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("has_axe")
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        ws.set("has_axe", true)
        return Status.SUCCESS
    }
}

class TestActionJoinSkyblock : ActionAbstract() {
    override val name: String = "join_skyblock"

    override val preConditions: Set<StateBelief> = setOf(
        StateBelief.of("in_hypixel"),
        StateBelief.of("in_lobby")
    )

    override val effects: Set<StateBelief> = setOf(
        StateBelief.of("in_skyblock", true),
        StateBelief.of("in_lobby", false)
    )

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_skyblock") && !ws.get("in_lobby")
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        ws.set("in_skyblock", true)
        return Status.SUCCESS
    }
}

class TestActionWarpGarden : ActionAbstract() {
    override val name: String = "warp_to_garden"

    override val preConditions: Set<StateBelief> = setOf(StateBelief.of("in_skyblock"))

    override val effects: Set<StateBelief> = setOf(StateBelief.of("in_garden"))

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_garden")
    }

    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        return if (ws.get("in_garden")) Status.SUCCESS else Status.RUNNING
    }
}

class TestActionWarpCrimson : ActionAbstract() {
    override val name: String = "warp_to_crimson"

    override val cost: Float = 1f

    override val preConditions: Set<StateBelief> = setOf(StateBelief.of("in_skyblock"))

    override val effects: Set<StateBelief> = setOf(StateBelief.of("in_crimson"))

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_crimson")
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        return if (ws.get("in_crimson")) Status.SUCCESS else Status.RUNNING
    }
}

class TestActionWalkKuudra : ActionAbstract() {
    override val name: String = "walk_kuudra"

    override val cost: Float = 10f

    override val preConditions: Set<StateBelief> = setOf(
        StateBelief.of("in_skyblock"),
        StateBelief.of("in_crimson")
    )

    override val effects: Set<StateBelief> = setOf(StateBelief.of("in_kuudra"))

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_kuudra")
    }

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        return Status.RUNNING
    }
}

class TestActionWarpKuudra : ActionAbstract() {
    override val name: String = "warp_kuudra"

    override val cost: Float = 1f

    override val preConditions: Set<StateBelief> = setOf(
        StateBelief.of("in_skyblock"),
        StateBelief.of("has_kuudra_warp")
    )

    override val effects: Set<StateBelief> = setOf(StateBelief.of("in_kuudra"))

    override fun isCompleted(ai: Goap, bb: BlackBoard, ws: WorldState): Boolean {
        return ws.get("in_kuudra")
    }

    override fun init(ai: Goap, bb: BlackBoard, ws: WorldState) {}

    override fun tick(ai: Goap, bb: BlackBoard, ws: WorldState, ticksInAction: Int): Status {
        return if (ws.get("in_kuudra")) Status.SUCCESS else Status.RUNNING
    }
}
