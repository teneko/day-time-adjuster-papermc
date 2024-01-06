package io.github.teneko.daytimeadjuster

class TickTimeSeries(timeSpans: Collection<TickTimeSpan>) {
    val timeSpans = timeSpans
        .flatMap { timeSpan ->
            if (timeSpan.isCrossingZero) {
                buildList {
                    if (timeSpan.from != TickTime.DAY_END) {
                        add(
                            TickTimeSpan(
                                from = timeSpan.from,
                                to = TickTime.DAY_END,
                                dayTicks = timeSpan.nonCrossingTicks
                            )
                        )
                    }

                    if (timeSpan.to != TickTime.DAY_START) {
                        add(
                            TickTimeSpan(
                                from = TickTime.DAY_START,
                                to = timeSpan.to,
                                dayTicks = timeSpan.crossingTicks
                            )
                        )
                    }
                }
            } else {
                listOf(timeSpan)
            }
        }
        .sortedBy { timeSpan -> timeSpan.from.ticks }

    val isTimeSeries = this.timeSpans.isNotEmpty()

    val start by lazy {
        this.timeSpans.first()
    }

    val end by lazy {
        this.timeSpans.drop(1).fold(start) { prev, next ->
            require(next.from >= prev.to) {
                "A tick time span is overlapping with another tick time span"
            }
            next
        }
    }

    init {
        if (isTimeSeries) {
            run { start }
            run { end }
        }
    }
}