package me.abhigya.chuunicore.services.hologram.line

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.mutableStateOf
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BlockLine(
    item: MutableState<ItemStack>
) : ILine<ItemStack> {

    private val line: Line = Line(EntityType.DROPPED_ITEM)
    private val resetVelocity = WrapperPlayServerEntityVelocity(entityId, Vector3d.zero())

    private val mutableState = item

    private var firstRender = true

    constructor(item: ItemStack) : this(mutableStateOf(item))

    override val type: ILine.Type get() = ILine.Type.BLOCK_LINE

    override val entityId: Int get() = line.entityID

    override val location: Location? get() = line.location

    override var obj: ItemStack
        get() = mutableState.get()
        set(value) = mutableState.set(value)

    override var pvt = ILine.PrivateConfig(this)

    override fun setLocation(value: Location) {
        line.location = value
    }

    override fun hide(player: Player) {
        line.destroy(player)
    }

    override fun teleport(player: Player) {
        line.teleport(player)
    }

    override fun show(player: Player) {
        line.spawn(player)
        this.update(player)

        resetVelocity.send(player)

        if(firstRender) {
            firstRender = false
            mutableState.addObserver(pvt)
        }
    }

    override fun update(player: Player) {
        WrapperPlayServerEntityMetadata(
            entityId,
            listOf(
                EntityData(5, EntityDataTypes.BOOLEAN, true),
                EntityData(8, EntityDataTypes.ITEMSTACK, obj)
            )
        ).send(player)
    }

}