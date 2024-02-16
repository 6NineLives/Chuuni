package me.abhigya.chuunicore.services.ui.thirdPerson

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.*
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.geometry.*
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.ui.UI
import me.abhigya.chuunicore.services.ui.UIButton
import me.abhigya.chuunicore.services.ui.UIElement
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

abstract class ThirdPersonUI(
    val host: Player,
    val location: Pos3D,
    val direction: Direction
) : UI {

    abstract val world: World
    val frameDirection by lazy { host.eyeLocation.direction }
    protected var cursor: Cursor? = null
    private val buttons: MutableList<TPUIButton> = mutableListOf()
    override var isRendered: Boolean = false

    private var entity1: Int = -1
    private var entity2: Int = -1

    override suspend fun render() {
        val location = location.toLocation(world)
        location.direction = direction.toVector()

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
        isRendered = true
    }

    abstract fun setup()

    protected fun addButton(button: TPUIButton) {
        buttons.add(button)
    }

    override suspend fun dispose() {
        cursor?.remove()

        WrapperPlayServerDestroyEntities(entity1, entity2).send(host)
    }

    enum class Direction(
        val modX: Int,
        val modY: Int,
        val modZ: Int
    ) {
        NORTH(0, 0, -1),
        EAST(1, 0, 0),
        SOUTH(0, 0, 1),
        WEST(-1, 0, 0),
        ;

        fun toVector(): Vector = Vector(modX.toDouble(), modY.toDouble(), modZ.toDouble())
    }

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
    elementDistanceMultiplier: Double = 0.52,
    override var onClick: suspend () -> Unit = {}
) : TPUIButton {

    private val _location: MutableState<Pos3D>
    override val location: Pos3D get() = _location.get()

    final override val host: Player get() = ui.host

    init {
        val center = host.location.toPos3D() + (ui.frameDirection.toPos3D() * elementDistanceMultiplier)
        // positive x north, positive z west
        val delta = center - host.location.toPos3D()
        _location = mutableStateOf(when {
            delta.x > 0 -> center + Pos3D(0.0, relativeLocation.y, relativeLocation.x)
            delta.x < 0 -> center + Pos3D(0.0, relativeLocation.y, -relativeLocation.x)
            delta.z > 0 -> center + Pos3D(-relativeLocation.x, relativeLocation.y, 0.0)
            delta.z < 0 -> center + Pos3D(relativeLocation.x, relativeLocation.y, 0.0)
            else -> center
        })
    }
}