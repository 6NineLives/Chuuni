package me.abhigya.chuunicore.services.chat

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.hologram.Hologram
import me.abhigya.chuunicore.services.hologram.HologramPool
import me.abhigya.chuunicore.services.hologram.Page
import me.abhigya.chuunicore.services.hologram.Text
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import toothpick.ktp.extension.getInstance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SpeechBubble(
    val pool: HologramPool = ChuuniCorePlugin.getPlugin().scope.getInstance(),
    val duration: Duration = 2.seconds,
    val updateDelay: Duration = 100.milliseconds,
    val offsetY: Float = 0.4F
) {

    companion object {
        val DEFAULT: SpeechBubble = SpeechBubble()
    }

    suspend fun bubble(entity: Entity, message: Component) = bubble(entity, mutableStateOf(message))

    suspend fun bubble(entity: Entity, message: MutableState<Component>): Bubble = coroutineScope {
        val location = { entity.location.add(0.0, entity.height + offsetY, 0.0) }
        val key = "${entity.entityId}-speech-bubble"
        pool.holograms[key]?.destroy()

        val hologram = Hologram(key, location(), pool) {
            Page {
                Text(message.get())
            }
        }
        hologram.showNearby()
        hologram.changeViewerPage(0)

        message.addObserver { _, new ->
            hologram.hologramPages[0].clearLines()
            hologram.hologramPages[0].addTextLine(new)
        }

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

    suspend fun bubbleNoFollow(entity: Entity, message: Component) = bubbleNoFollow(entity, mutableStateOf(message))

    suspend fun bubbleNoFollow(entity: Entity, message: MutableState<Component>): Bubble = coroutineScope {
        val key = "${entity.entityId}-speech-bubble"
        pool.holograms[key]?.destroy()

        val hologram = Hologram(key, entity.location.add(0.0, entity.height + offsetY, 0.0), pool) {
            Page {
                Text(message.get())
            }
        }
        hologram.showNearby()
        hologram.changeViewerPage(0)

        message.addObserver { _, new ->
            hologram.hologramPages[0].clearLines()
            hologram.hologramPages[0].addTextLine(new)
        }

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