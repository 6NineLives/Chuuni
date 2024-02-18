package me.abhigya.chuunicore.services.hologram

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.abhigya.chuunicore.model.ClickType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import java.util.concurrent.TimeUnit

class HologramListener(
    private val pool: HologramPool
) : PacketListenerAbstract(), Listener {

    private val cooldown: Cache<UUID, Int> = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MILLISECONDS).build();

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.INTERACT_ENTITY) return
        val player = event.player as Player

        val packet = WrapperPlayClientInteractEntity(event)
        if (cooldown.asMap().getOrDefault(player.uniqueId, -1) == packet.entityId) return

        for (hologram in pool.holograms.values) {
            if (!hologram.isClickRegistered) continue
            if (hologram.location.world != player.world) continue

            val page = hologram.hologramPages[hologram.viewerPages.getOrDefault(player.uniqueId, 0)]
            val line = page.lines.firstOrNull { packet.entityId in it.entityIds } ?: continue

            val clickType = packet.action.clickType(player)
            hologram.clickActions.forEach { it.onClick(player, clickType) }
            page.clickActions.forEach { it.onClick(player, clickType) }
            line.clickActions.forEach { it.onClick(player, clickType) }
            cooldown.put(player.uniqueId, packet.entityId)
            return
        }
    }

    private fun WrapperPlayClientInteractEntity.InteractAction.clickType(player: Player): ClickType {
        return if (this == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            if (player.isSneaking) ClickType.SHIFT_LEFT_CLICK else ClickType.LEFT_CLICK
        } else {
            if (player.isSneaking) ClickType.SHIFT_RIGHT_CLICK else ClickType.RIGHT_CLICK
        }
    }

    @EventHandler
    fun PlayerMoveEvent.handlePlayerMove() {
        for (hologram in pool.holograms.values) {
            hologram.tracker.onMove(this)
        }
    }
}