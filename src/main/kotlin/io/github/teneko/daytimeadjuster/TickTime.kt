package io.github.teneko.daytimeadjuster

@JvmInline
value class TickTime(val ticks: Short) : Comparable<TickTime> {
    companion object {
        private const val MIN_VALUE: Short = 0
        private const val MAX_VALUE: Short = 24000

        val MIN = TickTime(MIN_VALUE)
        val DAY_START = TickTime(0)
        val DAY_TIME_START = TickTime(0)
        val NOON = TickTime(6000)
        val DAY_TIME_END = TickTime(12000)
        val DUSK = TickTime(12000)
        val NIGHT_TIME_START = TickTime(13000)
        val MIDNIGHT = TickTime(18000)
        val NIGHT_TIME_END = TickTime(23000)
        val DAWN = TickTime(23000)
        val DAY_END = TickTime(24000)
        val MAX = TickTime(MAX_VALUE)
    }

    init {
        require(ticks >= MIN_VALUE)
        require(ticks <= MAX_VALUE)
    }

    fun compareTo(other: Short): Int {
        return ticks.compareTo(other)
    }

    override fun compareTo(other: TickTime): Int {
        return ticks.compareTo(other.ticks)
    }
}