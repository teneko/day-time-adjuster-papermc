package io.github.teneko.daytimeadjuster

enum class TickTimeUnit(val time: TickTime) {
    DAY_START(TickTime.DAY_START),
    DAY_TIME_START(TickTime.DAY_TIME_START),
    NOON(TickTime.NOON),
    DAY_TIME_END(TickTime.DAY_TIME_END),
    DUSK(TickTime.DUSK),
    NIGHT_TIME_START(TickTime.NIGHT_TIME_START),
    MIDNIGHT(TickTime.MIDNIGHT),
    NIGHT_TIME_END(TickTime.NIGHT_TIME_END),
    DAWN(TickTime.DAWN),
    DAY_END(TickTime.DAY_END)
}