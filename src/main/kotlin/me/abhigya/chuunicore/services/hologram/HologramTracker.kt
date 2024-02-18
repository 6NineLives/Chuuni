package me.abhigya.chuunicore.services.hologram

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent

internal class HologramTracker(
    val hologram: Hologram
) {

    private var task: Job? = null

    fun start() {
        task = hologram.pool.plugin.launch {
            while (isActive) {
                hologram.outOfRenderDistance.removeIf { it !in hologram.viewers }
                for (viewer in hologram.viewers) {
                    val player = Bukkit.getPlayer(viewer) ?: continue
                    if (!canSee(player)) hologram.outOfRenderDistance.add(viewer)
                }
                delay(100)
            }
        }
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    internal fun onMove(event: PlayerMoveEvent) {
        task ?: return
        if (event.isCancelled) return

        if (event.player.uniqueId !in hologram.viewers || event.player.uniqueId !in hologram.outOfRenderDistance) return
        if (!canSee(event.player)) return

        hologram.show(event.player)
        hologram.outOfRenderDistance.remove(event.player.uniqueId)
    }

    private fun canSee(player: Player): Boolean {
        val vector = player.location.toVector().subtract(hologram.location.toVector())
        return vector.x >= -hologram.displayRange &&
                vector.x <= hologram.displayRange &&
                vector.z >= -hologram.displayRange &&
                vector.z <= hologram.displayRange
    }

}