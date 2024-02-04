package me.abhigya.chuunicore.ext

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import org.bukkit.entity.Player

fun <T : PacketWrapper<T>> PacketWrapper<T>.send(player: Player) {
    PacketEvents.getAPI().playerManager.sendPacket(player, this)
}