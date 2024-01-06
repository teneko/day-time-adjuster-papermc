package io.github.teneko.daytimeadjuster

import org.bukkit.Bukkit
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.plugin.java.JavaPlugin
import kotlin.streams.asSequence

@Suppress("unused")
const val MOD_ID = "day-time-adjuster"

const val TASK_INTERVAL = 1L

@Suppress("unused")
class DayTimeAdjusterPlugin : JavaPlugin() {
    private var dayTimeAdjusters: Collection<DayTimeAdjuster>? = null

    override fun onEnable() {
        saveDefaultConfig()

        if (!config.getBoolean("enabled")) {
            logger.info(
                "Plugin is disabled, because enabled = false. If not present, then enabled is implicitly false."
            )

            return
        }

        val worldsSection =
            config.getConfigurationSection("worlds") ?: throw ConfigException("Config-path 'worlds' does not exist'")

        dayTimeAdjusters = worldsSection
            .getKeys(false)
            .onEach { worldName ->
                logger.info("Found config for world '$worldName'")
            }
            .flatMap { worldName ->
                val worldSectionPath = "worlds.$worldName"

                val worldSection = config.getList(worldSectionPath)
                    ?: throw ConfigException("Config-path '$worldSectionPath' does not exist'")

                val timeSpans = worldSection.stream().asSequence()
                    .filterIsInstance<Map<String, *>>().map {
                        MemoryConfiguration().apply {
                            it.entries.forEach {
                                set(it.key, it.value)
                            }
                        }
                    }
                    .map {
                        TickTimeSpan.of(it)
                    }.onEach {
                        logger.info(
                            "[$worldName] Found config for adjusting time between ${it.from.ticks} and ${it.to.ticks}"
                        )
                    }
                    .toList()

                // Normalize crossing time spans
                val timeSeries = TickTimeSeries(timeSpans)
                val world = Bukkit.getWorld(worldName) ?: error("World '$worldName' does not exist")

                timeSeries.timeSpans
                    .map { between ->
                        // Adapter
                        fun addTicks(ticks: Long) {
                            world.time += ticks
                        }

                        DayTimeAdjuster(
                            between,
                            world::getTime,
                            ::addTicks,
                            TASK_INTERVAL,
                            traceId = "[$worldName ${between.from.ticks}-${between.to.ticks}]"
                        )
                    }
            }
            .onEach {
                logger.info(
                    "${it.traceId} Enable time adjuster"
                )
                it.runTaskTimer(this, 0, TASK_INTERVAL)
            }

        logger.info("Enabled plugin")
    }

    override fun onDisable() {
        dayTimeAdjusters?.onEach { dayTimeAdjuster ->
            dayTimeAdjuster.cancel()
        }
    }
}