package me.abhigya.chuunicore.services.ui.thirdPerson

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.*
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.geometry.*
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.ui.UI
import me.abhigya.chuunicore.services.ui.UIButton
import me.abhigya.chuunicore.services.ui.UIElement
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector
import java.util.*

abstract class ThirdPersonUI(
    val host: Player,
    val location: Pos3D,
    val direction: Direction,
    val dimension: Dimension
) : UI, Listener {

    internal val plugin: ChuuniCorePlugin = ChuuniCorePlugin.getPlugin()
    abstract val world: World
    protected var cursor: Cursor? = null
    internal val buttons: MutableList<TPUIButton> = mutableListOf()
    override var isRendered: Boolean = false

    private var entity1: Int = -1
    private var entity2: Int = -1

    override suspend fun render() {
        val location = location.toLocation(world)
        location.yaw = direction.yaw

        entity1 = SpigotReflectionUtil.generateEntityId()
        entity2 = SpigotReflectionUtil.generateEntityId()

        WrapperPlayServerSpawnEntity(
            entity1,
            UUID.randomUUID(),
            EntityTypes.ARMOR_STAND,
            SpigotConversionUtil.fromBukkitLocation(location),
            0.0f,
            0,
            Vector3d(0.0, 0.0, 0.0)
        ).send(host)
        WrapperPlayServerEntityMetadata(
            entity1,
            listOf(
                EntityData(0, EntityDataTypes.BYTE, 0x20.toByte()),
                EntityData(5, EntityDataTypes.BOOLEAN, true)
            )
        ).send(host)
        WrapperPlayServerEntityHeadLook(entity1, location.yaw).send(host)

        location.add(0.0, 5.0, 0.0)
        WrapperPlayServerSpawnEntity(
            entity2,
            UUID.randomUUID(),
            EntityTypes.ARMOR_STAND,
            SpigotConversionUtil.fromBukkitLocation(location),
            0.0f,
            0,
            Vector3d(0.0, 0.0, 0.0)
        ).send(host)
        WrapperPlayServerEntityMetadata(
            entity2,
            listOf(
                EntityData(0, EntityDataTypes.BYTE, 0x20.toByte()),
                EntityData(5, EntityDataTypes.BOOLEAN, true),
                EntityData(15, EntityDataTypes.BYTE, 0x10.toByte())
            )
        ).send(host)

        WrapperPlayServerSetPassengers(entity2, intArrayOf(host.entityId)).send(host)

        WrapperPlayServerCamera(entity1).send(host)
        WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, 3.0f).send(host)

        setup()

        cursor?.render()
        buttons.forEach { it.render() }
        plugin.server.pluginManager.registerEvents(this, plugin)

        isRendered = true
    }

    abstract fun setup()

    override suspend fun dispose() {
        HandlerList.unregisterAll(this)
        cursor?.remove()
        buttons.forEach { it.remove() }

        WrapperPlayServerDestroyEntities(entity1, entity2).send(host)
        entity2 = -1
        entity1 = -1
    }

    @EventHandler
    fun PlayerInteractEvent.handleInteract() {
        if (player != host) return
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return

        isCancelled = true
        cursor?.tryClick()
    }

    @EventHandler
    fun PlayerQuitEvent.handlePlayerQuit() {
        if (player != host) return
        plugin.launch { dispose() }
    }

    enum class Direction(
        val modX: Int,
        val modY: Int,
        val modZ: Int,
        val yaw: Float
    ) {
        NORTH(0, 0, -1, 180f),
        EAST(1, 0, 0, -90f),
        SOUTH(0, 0, 1, 0f),
        WEST(-1, 0, 0, 90f),
        ;

        fun toVector(): Vector = Vector(modX.toDouble(), modY.toDouble(), modZ.toDouble())

        val opposite: Direction get() = when (this) {
            NORTH -> SOUTH
            EAST -> WEST
            SOUTH -> NORTH
            WEST -> EAST
        }
    }

    // Dimension in block size
    data class Dimension(
        val width: Double,
        val height: Double
    )

}

interface TPUIElement : UIElement {

    override val ui: ThirdPersonUI

    val location: Pos3D

    val world: World get() = ui.world
    val host get() = ui.host

}

interface TPUIButton : TPUIElement, UIButton {

    val relativeLocation: Pos2D

}

abstract class AbstractTPUIButton(
    final override val ui: ThirdPersonUI,
    final override val relativeLocation: Pos2D,
    elementDistanceMultiplier: Double,
    override var onClick: () -> Unit = {}
) : TPUIButton {

    private val _location: MutableState<Pos3D>
    override val location: Pos3D get() = _location.get()

    final override val host: Player get() = ui.host

    init {
        require(elementDistanceMultiplier > 0.0) { "Element distance multiplier must be greater than 0" }
        require(relativeLocation.x in -(ui.dimension.width / 2)..(ui.dimension.width / 2)) { "Relative location x must be within the width of the UI" }
        require(relativeLocation.y in -(ui.dimension.height / 2)..(ui.dimension.height / 2)) { "Relative location y must be within the height of the UI" }

        val center = ui.location + (ui.direction.toVector().toPos3D() * elementDistanceMultiplier)
        _location = mutableStateOf(when (ui.direction) {
            ThirdPersonUI.Direction.NORTH -> center + Pos3D(0.0, relativeLocation.y, relativeLocation.x)
            ThirdPersonUI.Direction.SOUTH -> center + Pos3D(0.0, relativeLocation.y, -relativeLocation.x)
            ThirdPersonUI.Direction.WEST -> center + Pos3D(-relativeLocation.x, relativeLocation.y, 0.0)
            ThirdPersonUI.Direction.EAST -> center + Pos3D(relativeLocation.x, relativeLocation.y, 0.0)
        })
    }
}