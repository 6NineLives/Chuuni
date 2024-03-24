package me.abhigya.chuunicore.services.hologram

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.player.Equipment
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.ext.send
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import toothpick.InjectConstructor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.experimental.or

const val HOLOGRAM_DEFAULT_DISPLAY_RANGE: Int = 48

@InjectConstructor
class HologramPool(
    internal val plugin: ChuuniCorePlugin
) {

    val holograms: MutableMap<String, Hologram> = ConcurrentHashMap()
    private val listener: HologramListener = HologramListener(this)
    private var task: Job? = null

    fun init() {
        plugin.server.pluginManager.registerEvents(listener, plugin);
        PacketEvents.getAPI().eventManager.registerListeners(listener)

        task = plugin.launch {
            while (true) {
                for (value in holograms.values) {
                    if (!value.isUpdateRegistered) continue
                    if (System.currentTimeMillis() - value.lastUpdate < value.updateInterval.inWholeMilliseconds) continue
                    if (value.hasChangedContentType) {
                        updateHologram(value)
                    } else {
                        updateContent(value)
                    }
                }

                delay(100)
            }
        }
    }

    internal fun spawnHologram(hologram: Hologram, player: Player) {
        if (!hologram.isVisible(player)) return
        val page = hologram.getCurrentPage(player) ?: return
        val map = mapLocations(page)

        for ((line, location) in map) {
            val peLocation = SpigotConversionUtil.fromBukkitLocation(location)
            WrapperPlayServerSpawnEntity(
                line.entityIds[0],
                UUID.randomUUID(),
                EntityTypes.ARMOR_STAND,
                peLocation,
                location.yaw,
                0,
                Vector3d.zero()
            ).send(player)

            WrapperPlayServerEntityMetadata(
                line.entityIds[0],
                buildList {
                    add(EntityData(0, EntityDataTypes.BYTE, 0x20.toByte()))

                    var b = 0x08.toByte()
                    if (line !is HologramLine.Head) b = b or 0x01.toByte()
                    b = b or 0x10.toByte()

                    add(EntityData(10, EntityDataTypes.BYTE, b))
                }
            ).send(player)

            when (line) {
                is HologramLine.Text -> {
                    WrapperPlayServerEntityMetadata(
                        line.entityIds[0],
                        listOf(
                            EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(line.content)),
                            EntityData(3, EntityDataTypes.BOOLEAN, true)
                        )
                    ).send(player)
                }
                is HologramLine.Head, is HologramLine.SmallHead -> {
                    WrapperPlayServerEntityEquipment(
                        line.entityIds[0],
                        listOf(
                            Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(line.content as ItemStack))
                        )
                    ).send(player)
                }
                is HologramLine.Icon -> {
                    WrapperPlayServerSpawnEntity(
                        line.entityIds[1],
                        UUID.randomUUID(),
                        EntityTypes.ITEM,
                        peLocation,
                        0f,
                        0,
                        Vector3d.zero()
                    ).send(player)

                    WrapperPlayServerAttachEntity(
                        line.entityIds[1],
                        line.entityIds[0],
                        false
                    ).send(player)
                }
                is HologramLine.Entity -> {
                    WrapperPlayServerSpawnEntity(
                        line.entityIds[1],
                        UUID.randomUUID(),
                        line.content.type,
                        peLocation,
                        0f,
                        0,
                        Vector3d.zero()
                    ).send(player)

                    if (line.content.isBaby) {
                        WrapperPlayServerEntityMetadata(
                            line.entityIds[1],
                            listOf(
                                EntityData(16, EntityDataTypes.BOOLEAN, true)
                            )
                        ).send(player)
                    }
                }
            }
        }
    }

    fun createHologram(key: String, location: Location): Hologram {
        val hologram = Hologram(key, this, location)
        holograms[key] = hologram
        return hologram
    }

    internal fun updateHologram(hologram: Hologram) {
        for (uuid in hologram.viewerPages.keys) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            updateContent(hologram, player)
            updateLocation(hologram, player)
            hologram.lastUpdate = System.currentTimeMillis()
        }
        hologram.hasChangedContentType = false
    }

    internal fun despawnHologran(hologram: Hologram, player: Player) {
        val page = hologram.getCurrentPage(player) ?: return
        for (line in page.lines) {
            WrapperPlayServerDestroyEntities(
                *line.entityIds.toIntArray()
            ).send(player)
        }
    }

    internal fun respawnHologram(hologram: Hologram, beforeRespawn: () -> Unit = {}) {
        for (uuid in hologram.viewerPages.keys) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            despawnHologran(hologram, player)
        }
        beforeRespawn()
        for (uuid in hologram.viewerPages.keys) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            spawnHologram(hologram, player)
        }
        hologram.hasChangedContentType = false
    }

    internal fun respawnHologram(hologram: Hologram, player: Player, beforeRespawn: () -> Unit = {}) {
        despawnHologran(hologram, player)
        beforeRespawn()
        spawnHologram(hologram, player)
    }

    internal fun updateContent(hologram: Hologram) {
        for (uuid in hologram.viewerPages.keys) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            updateContent(hologram, player)
        }
    }

    internal fun updateContent(hologram: Hologram, player: Player) {
        if (!hologram.isVisible(player)) return
        val page = hologram.hologramPages[hologram.viewerPages[player.uniqueId] ?: return]
        for (line in page.lines) {
            if (line.content is Component) {
                WrapperPlayServerEntityMetadata(
                    line.entityIds[0],
                    listOf(
                        EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(line.content as Component)),
                        EntityData(3, EntityDataTypes.BOOLEAN, true)
                    )
                ).send(player)
            }
        }
    }

    internal fun updateLocation(hologram: Hologram) {
        for (uuid in hologram.viewerPages.keys) {
            val player = plugin.server.getPlayer(uuid) ?: continue
            updateLocation(hologram, player)
        }
    }

    internal fun updateLocation(hologram: Hologram, player: Player) {
        val page = hologram.hologramPages[hologram.viewerPages[player.uniqueId] ?: return]
        val map = this.mapLocations(page)
        for ((line, location) in map) {
            for (entityId in line.entityIds) {
                WrapperPlayServerEntityTeleport(
                    entityId,
                    SpigotConversionUtil.fromBukkitLocation(location),
                    true
                ).send(player)
            }
        }
    }

    private fun mapLocations(page: HologramPage): List<Pair<HologramLine<*>, Location>> {
        val returnMap: MutableList<Pair<HologramLine<*>, Location>> = ArrayList()
        var l = page.parent.location
        val usedLines = if (page.parent.isInverted) page.lines.reversed() else page.lines
        for (line in usedLines) {
            returnMap.add(line to l)
            l = l.add(0.0, (line.height + page.lineGap).toDouble(), 0.0).clone()
        }
        return returnMap
    }

    fun dispose() {
        holograms.values.forEach { it.destroy() }
        holograms.clear()
        task?.cancel()
        task = null
        PacketEvents.getAPI().eventManager.unregisterListeners(listener)
        HandlerList.unregisterAll(listener)
    }

}