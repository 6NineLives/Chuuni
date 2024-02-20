package me.abhigya.chuunicore.services.ui.thirdPerson

import com.github.retrooper.packetevents.protocol.nbt.*
import com.github.retrooper.packetevents.protocol.world.TileEntityType
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.geometry.Pos2D
import me.abhigya.chuunicore.model.geometry.toBlockPos
import me.abhigya.chuunicore.model.mutableStateOf
import net.kyori.adventure.text.Component
import org.bukkit.DyeColor

class SignButton(
    ui: ThirdPersonUI,
    val text: MutableState<Array<Component>>,
    val color: MutableState<DyeColor> = mutableStateOf(DyeColor.BLACK),
    val isGlowing: MutableState<Boolean> = mutableStateOf(false),
    relativeLocation: Pos2D,
    elementDistanceMultiplier: Double = 5.0,
    onClick: () -> Unit
) : AbstractTPUIButton(ui, relativeLocation, elementDistanceMultiplier, onClick) {

    override var isRendered: Boolean = false

    override suspend fun render() {
        val loc = location.toBlockPos().let { Vector3i(it.x, it.y, it.z) }
        val blockState = StateTypes.OAK_WALL_SIGN.createBlockState()
        val blockChange = WrapperPlayServerBlockChange(loc, blockState.globalId)
        val metaData = WrapperPlayServerBlockEntityData(loc, 7, getSignData())
        blockChange.send(host)
        metaData.send(host)

        text.addObserver { _, _ -> WrapperPlayServerBlockEntityData(loc, 7, getSignData()).send(host) }
        color.addObserver { _, _ -> WrapperPlayServerBlockEntityData(loc, 7, getSignData()).send(host) }
        isGlowing.addObserver { _, _ -> WrapperPlayServerBlockEntityData(loc, 7, getSignData()).send(host) }

        isRendered = true
    }

    override suspend fun remove() {
        val blockChange = WrapperPlayServerBlockChange(location.toBlockPos().let { Vector3i(it.x, it.y, it.z) }, 0)
        blockChange.send(host)
        isRendered = false
    }

    private fun getSignData(): NBTCompound {
        val text = text.get()
        val nbt = NBTCompound()
        val frontText = NBTCompound()
        frontText.setTag("messages", NBTList(NBTType.STRING).apply {
            repeat(4) {
                addTag(NBTString(AdventureSerializer.toJson(text.getOrElse(it) { Component.empty() })))
            }
        })

        frontText.setTag("color", NBTString(color.get().name))
        frontText.setTag("has_glowing_text", NBTByte(if (isGlowing.get()) 1 else 0))
        nbt.setTag("front_text", frontText)

        val backText = NBTCompound()
        backText.setTag("messages", NBTList(NBTType.STRING).apply {
            val empty = AdventureSerializer.toJson(Component.empty())
            repeat(4) {
                addTag(NBTString(empty))
            }
        })
        backText.setTag("color", NBTString("BLACK"))
        backText.setTag("has_glowing_text", NBTByte(0))
        nbt.setTag("back_text", backText)

        nbt.setTag("is_waxed", NBTByte(0))
        return nbt
    }

}