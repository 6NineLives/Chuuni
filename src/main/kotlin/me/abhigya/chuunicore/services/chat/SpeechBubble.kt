package me.abhigya.chuunicore.services.chat

import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.features.Feature
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.hologram.*
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import toothpick.ktp.extension.getInstance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SpeechBubble(
    private val pool: HologramPool = ChuuniCorePlugin.getPlugin().scope.getInstance(),
    private val duration: Duration = 2.seconds,
    private val updateDelay: Duration = 100.milliseconds,
    private val offsetY: Float = 0.4F,
    private val displayRange: Int = HOLOGRAM_DEFAULT_DISPLAY_RANGE
) {

    companion object Service : Feature {
        private val componentObserver: (Hologram) -> Observer<Component> = {
            Observer { _, new ->
                it.hologramPages[0].run {
                    clearLines()
                    addTextLine(new)
                }
            }
        }

        val DEFAULT: SpeechBubble = SpeechBubble()

        private var listener: SpeechBubbleListener? = null

        override val isEnabled: Boolean = listener != null

        override suspend fun enable(plugin: ChuuniCorePlugin) {
            listener = SpeechBubbleListener(plugin, DEFAULT).also {
                plugin.server.pluginManager.registerEvents(it, plugin)
            }
        }

        override suspend fun disable(plugin: ChuuniCorePlugin) {
            listener?.let {
                HandlerList.unregisterAll(it)
            }
            listener = null
        }
    }

    suspend fun bubble(entity: Entity, message: MutableState<Component>) = bubble(entity, message, entity.location.getNearbyPlayers(displayRange.toDouble()))

    suspend fun bubble(entity: Entity, message: MutableState<Component>, audience: Iterable<Player>): Bubble = coroutineScope {
        val location = { entity.location.add(0.0, entity.height + offsetY, 0.0) }
        val key = "${entity.entityId}-speech-bubble"
        pool.holograms[key]?.destroy()

        val hologram = Hologram(key, location(), pool) {
            Page {
                Text(message.get())
            }
        }

        hologram.displayRange = displayRange
        hologram.updateRange = displayRange

        for (player in audience) {
            hologram.show(player)
        }
        hologram.changeViewerPage(0)

        message.addObserver(componentObserver(hologram))

        val job = launch {
            val start = System.currentTimeMillis()
            val millis = duration.inWholeMilliseconds
            while (System.currentTimeMillis() - start < millis && !entity.isDead && entity.isValid) {
                delay(updateDelay)
                hologram.teleport(location())
            }
        }.also {
            it.invokeOnCompletion {
                hologram.destroy()
            }
        }

        Bubble(entity, job, hologram)
    }

    suspend fun bubbleNoFollow(entity: Entity, message: MutableState<Component>) = bubbleNoFollow(entity, message, entity.location.getNearbyPlayers(displayRange.toDouble()))

    suspend fun bubbleNoFollow(entity: Entity, message: MutableState<Component>, audience: Iterable<Player>): Bubble = coroutineScope {
        val key = "${entity.entityId}-speech-bubble"
        pool.holograms[key]?.destroy()

        val hologram = Hologram(key, entity.location.add(0.0, entity.height + offsetY, 0.0), pool) {
            Page {
                Text(message.get())
            }
        }

        hologram.displayRange = displayRange
        hologram.updateRange = displayRange

        for (player in audience) {
            hologram.show(player)
        }
        hologram.changeViewerPage(0)

        message.addObserver(componentObserver(hologram))

        val job = launch {
            delay(duration)
        }.also {
            it.invokeOnCompletion {
                hologram.destroy()
            }
        }

        Bubble(entity, job, hologram)
    }

    data class Bubble(
        val entity: Entity,
        val task: Job,
        val hologram: Hologram
    ) {

        fun destroy() {
            task.cancel()
            hologram.destroy()
        }

    }

}

internal class SpeechBubbleListener(
    private val plugin: ChuuniCorePlugin,
    private val speechBubble: SpeechBubble
) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun AsyncChatEvent.handlePlayerChat() {
        if (isCancelled) return
        plugin.launch {
            speechBubble.bubble(player, mutableStateOf(message()))
        }
    }
}