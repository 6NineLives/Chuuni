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
import me.abhigya.chuunicore.services.hologram.IHologramLoader
import me.abhigya.chuunicore.services.hologram.TextBlockStandardLoader
import me.abhigya.chuunicore.services.hologram.line.TextLine
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
    val offsetY: Float = 0.4F,
    val hologramLoader: IHologramLoader = TextBlockStandardLoader
) {

    companion object {
        val DEFAULT: SpeechBubble = SpeechBubble()
    }

    suspend fun bubble(entity: Entity, message: Component) = bubble(entity, mutableStateOf(message))

    suspend fun bubble(entity: Entity, message: MutableState<Component>): Bubble = coroutineScope {
        val location = { entity.location.add(0.0, entity.height + offsetY, 0.0) }
        val key = "${entity.entityId}-speech-bubble"
        pool.remove(key)

        val hologram = Hologram(pool) {
            this.location = location()
            this.key = key
            this.loader = hologramLoader

            text(message.get())
        }
        hologram.showNearby()

        message.addObserver { _, new ->
            if (hologram.seeingPlayers.isNotEmpty()) {
                hologram.load(TextLine(new, null, false))
            }
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
        pool.remove(key)

        val hologram = Hologram(pool) {
            this.location = entity.location.add(0.0, entity.height + offsetY, 0.0)
            this.key = key
            this.loader = hologramLoader

            text(message.get())
        }
        hologram.showNearby()

        message.addObserver { _, new ->
            if (hologram.seeingPlayers.isNotEmpty()) {
                hologram.load(TextLine(new, null, false))
            }
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
            if (hologram.seeingPlayers.isNotEmpty()) {
                hologram.destroy()
            }
        }

    }

}