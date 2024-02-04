package me.abhigya.chuunicore.services.hologram.line

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import me.abhigya.chuunicore.ext.send
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

class Line(private val entityType: EntityType, var location: Location? = null) {

    val entityID: Int = SpigotReflectionUtil.generateEntityId()
    private val entityDestroyPacket: WrapperPlayServerDestroyEntities = WrapperPlayServerDestroyEntities(entityID)

    fun destroy(player: Player) {
        entityDestroyPacket.send(player)
    }

    fun spawn(player: Player) {
        WrapperPlayServerSpawnEntity(
            entityID,
            UUID.randomUUID(),
            SpigotConversionUtil.fromBukkitEntityType(entityType),
            SpigotConversionUtil.fromBukkitLocation(location ?: throw RuntimeException("Forgot the location?")),
            0.0F,
            0,
            null
        ).send(player)
    }

    fun teleport(player: Player) {
        WrapperPlayServerEntityTeleport(
            entityID,
            SpigotConversionUtil.fromBukkitLocation(location ?: throw RuntimeException("Forgot the location?")),
            true
        ).send(player)
    }


}