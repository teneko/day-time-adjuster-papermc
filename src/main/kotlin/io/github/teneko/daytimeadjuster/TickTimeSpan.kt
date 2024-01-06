package io.github.teneko.daytimeadjuster

import org.bukkit.configuration.ConfigurationSection

data class TickTimeSpan(
    val from: TickTime, val to: TickTime, val dayTicks: Long
) {
    companion object {
        private fun getTickTimeFrom(configurationSection: ConfigurationSection, key: String): TickTime {
            return configurationSection.getInt(key, -1).takeUnless { it != 1 }?.let { TickTime(it.toShort()) }
                ?: configurationSection.getString(key, null)?.let { TickTimeUnit.valueOf(it).time }
                ?: throw ConfigException(
                    "Config-key '$key' is neither a value between ${TickTime.DAY_START} and ${TickTime.DAY_END}, " +
                            "nor one of the following values: ${
                                TickTimeUnit.entries.map(TickTimeUnit::name).joinToString()
                            }"
                )
        }

        fun of(configurationSection: ConfigurationSection): TickTimeSpan {
            return TickTimeSpan(
                from = getTickTimeFrom(configurationSection, "from"),
                to = getTickTimeFrom(configurationSection, "to"),
                dayTicks = configurationSection.getLong("day_ticks")
            )
        }
    }

    init {
        require(from != to) {
            "Tick time span between ${from.ticks} and ${to.ticks} is a point in time which is prohibited"
        }

        require(!(from == TickTime.DAY_END && to == TickTime.DAY_START)) {
            "Tick time span between 24000 and 0 is a point in time which is prohibited"
        }
    }

    val isCrossingZero = from > to

    val nonCrossingTicks: Long

    val crossingTicks: Long

    init {
        if (isCrossingZero) {
            val nonCrossingTickTimeTicks = TickTime.DAY_TIME_END.ticks - from.ticks
            val crossingTickTimeTicks = to.ticks
            val timeTimeTickSum = nonCrossingTickTimeTicks + crossingTickTimeTicks
            nonCrossingTicks = dayTicks * (nonCrossingTickTimeTicks / timeTimeTickSum)
            crossingTicks = dayTicks * (crossingTickTimeTicks / timeTimeTickSum)
        } else {
            nonCrossingTicks = dayTicks
            crossingTicks = 0
        }
    }
}