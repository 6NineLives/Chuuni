package me.abhigya.chuunicore.services.hologram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.ext.scheduleTickTask
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

data class HologramPoolOptions(
    val spawnDistance: Double = 60.0,
    val minHitDistance: Float = 0.5F,
    val maxHitDistance: Float = 5F,
)

class KeyAlreadyExistsException(key: HologramKey) : IllegalStateException("Key '$key' already exists")

class NoValueForKeyException(key: String) : IllegalStateException("No value for key '$key'")

class HologramPool(
    val options: HologramPoolOptions = HologramPoolOptions(),
    private val coroutineScope: CoroutineScope
) {

    internal val holograms: MutableMap<HologramKey, Hologram> = ConcurrentHashMap()
    private val listener: HologramListener = HologramListener(this, coroutineScope)
    private var task: Job? = null

    init {
        require(options.minHitDistance >= 0) { "minHitDistance must be positive" }
        require(options.maxHitDistance <= 120) { "maxHitDistance cannot be greater than 120" }
    }

    fun get(key: HologramKey): Hologram {
        return holograms[key] ?: throw NoValueForKeyException(key.id)
    }

    fun get(keyId: String) : Hologram {
        for((key, holo) in holograms) {
            if(key.id == keyId) {
                return holo
            }
        }
        throw NoValueForKeyException(keyId)
    }

    fun takeCareOf(key: HologramKey, value: Hologram) {
        if (holograms.containsKey(key)) {
            throw KeyAlreadyExistsException(key)
        }
        holograms[key] = value
    }

    fun remove(key: String): Hologram? = remove(HologramKey(key, this))

    fun remove(key: HologramKey): Hologram? {
        // if removed
        val removed = holograms.remove(key)
        removed?.let {
            for (player in it.seeingPlayers) {
                it.hide(player)
            }
            return it
        }
        return null
    }

    fun init(plugin: Plugin) {
        Bukkit.getPluginManager().registerEvents(listener, plugin)
        task = coroutineScope.launch {
            while (true) {
                for (player in Bukkit.getOnlinePlayers()) {
                    for (hologram in holograms.values) {
                        val holoLoc = hologram.location
                        val playerLoc = player.location
                        val isShown = hologram.isShownFor(player)

                        if (holoLoc.world != playerLoc.world) {
                            if (isShown) {
                                hologram.hide(player)
                            }
                            continue
                        } else if (!holoLoc.world.isChunkLoaded(holoLoc.blockX shr 4, holoLoc.blockZ shr 4) && isShown) {
                            hologram.hide(player)
                            continue
                        }
                        val inRange = holoLoc.distanceSquared(playerLoc) <= options.spawnDistance

                        if (!inRange && isShown) {
                            hologram.hide(player)
                        } else if (inRange && !isShown) {
                            hologram.show(player)
                        }
                    }
                }

                delay(100)
            }
        }
    }

    fun cleanUp() {
        HandlerList.unregisterAll(listener)
        task?.cancel()
        for (hologram in holograms.values) {
            for (player in hologram.seeingPlayers) {
                hologram.hide(player)
            }
        }
        holograms.clear()
    }

}