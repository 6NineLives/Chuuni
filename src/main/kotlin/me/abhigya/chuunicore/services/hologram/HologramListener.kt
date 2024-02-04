package me.abhigya.chuunicore.services.hologram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.services.hologram.line.ILine
import me.abhigya.chuunicore.services.hologram.line.TextLine
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class HologramListener(
    private val pool: HologramPool,
    private val coroutineScope: CoroutineScope
) : Listener {

    @EventHandler
    fun PlayerRespawnEvent.handlePlayerRespawn() {
        pool.holograms.values.asSequence()
            .filter { it.isShownFor(player) }
            .forEach { it.hide(player) }
    }

    @EventHandler
    fun PlayerQuitEvent.handlePlayerQuit() {
        pool.holograms.values.asSequence()
            .filter { it.isShownFor(player) }
            .forEach { it.seeingPlayers.remove(player) }
    }

    @EventHandler
    fun PlayerInteractEvent.handlePlayerInteract() {
        coroutineScope.launch {
            if (action != Action.LEFT_CLICK_AIR) return@launch

            FST@ for (hologram in pool.holograms.values) {
                if (!hologram.isShownFor(player)) continue

                for (line in hologram.lines) {
                    if (line.type != ILine.Type.TEXT_LINE) continue

                    val tL = line as TextLine
                    if (!tL.clickable) continue
                    if (tL.hitbox == null) continue

                    val result = tL.hitbox!!.rayTrace(player.eyeLocation.toVector(), player.location.direction, pool.options.maxHitDistance.toDouble())
                    if (player.eyeLocation.toVector().subtract(result!!.hitPosition).length() > pool.options.minHitDistance) {
                        continue
                    }

                    tL.click(player)
                    break@FST
                }
            }
        }
    }

}