package io.github.teneko.daytimeadjuster

import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.floor

class DayTimeAdjuster(
    val between: TickTimeSpan,
    private val currentTime: () -> Long,
    private val addTicks: (Long) -> Unit,
    private val runningTickInterval: Long,
    val traceId: String,
) : BukkitRunnable() {
    init {
        require(!between.isCrossingZero) {
            "Tick time span is crossing zero"
        }
    }

    private val slowDownTime: Boolean
    private val speedUpTime: Boolean
    private val ticksScaleFactor: Double
    private var ticksBottle: Double

    init {
        if (between.dayTicks > TickTime.MAX.ticks.toLong()) {
            slowDownTime = true
            speedUpTime = false
            ticksScaleFactor = (between.dayTicks / TickTime.MAX.ticks.toDouble()) * runningTickInterval
            ticksBottle = ticksScaleFactor
        } else if (between.dayTicks < TickTime.MAX.ticks.toLong()) {
            speedUpTime = true
            slowDownTime = false
            ticksScaleFactor = (TickTime.MAX.ticks.toDouble() / between.dayTicks) * runningTickInterval
            ticksBottle = 0.0
        } else {
            speedUpTime = false
            slowDownTime = false
            ticksScaleFactor = 0.0
            ticksBottle = 0.0
        }
    }

    private fun adjustTime() {
        if (slowDownTime) {
            ticksBottle -= runningTickInterval

            if (ticksBottle > 0) {
                addTicks(-runningTickInterval) // Freeze time
            } else {
                ticksBottle += ticksScaleFactor
            }
        } else if (speedUpTime) {
            ticksBottle += -runningTickInterval + (runningTickInterval * ticksScaleFactor)
            val additionalTicks = floor(ticksBottle).toLong()
            ticksBottle -= additionalTicks

            if (additionalTicks > 0) {
                addTicks(additionalTicks)
            }
        }
    }

    override fun run() {
        val currentTime = currentTime()

        if (currentTime in between.from.ticks..between.to.ticks) {
            adjustTime()
        }
    }
}