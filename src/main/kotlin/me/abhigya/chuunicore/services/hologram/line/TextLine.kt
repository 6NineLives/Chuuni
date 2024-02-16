package me.abhigya.chuunicore.services.hologram.line

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.services.hologram.ClickEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import java.util.Optional

class TextLine(
    override var obj: Component,
    private val args: Array<*>? = null,
    val clickable: Boolean = false
) : ILine<Component> {

    private val line: Line = Line(EntityType.ARMOR_STAND)
    private var clickEvent: ClickEvent = ClickEvent { }
    private var firstRender: Boolean = true

    var hitbox: BoundingBox? = null
        private set

    private var isEmpty = false

    private fun parse(): Component {
        if (args == null) {
            return obj
        }

        val res = Array(args.size) { i ->
            val tmp = args[i]
            if (tmp is MutableState<*>) {
                val value = tmp.get()
                if (firstRender) {
                    firstRender = false
                    (tmp as MutableState<Component>).addObserver(pvt)
                }
                value
            } else {
                tmp
            }
        }

        return obj.replaceText {
            var i = 0
            it.times(res.size)
                .matchLiteral("{}")
                .replacement { _ ->
                    val value = res[i++]
                    if (value is Component) {
                        value
                    } else {
                        Component.text(value.toString())
                    }
                }
        }
    }

    fun onClick(clickEvent: ClickEvent) {
        this.clickEvent = clickEvent
    }

    fun click(player: Player) {
        clickEvent.onClick(player)
    }

    override val type: ILine.Type get() = ILine.Type.TEXT_LINE

    override val entityId: Int get() = line.entityID

    override val location: Location? get() = line.location

    override var pvt = ILine.PrivateConfig(this)

    override fun setLocation(value: Location) {
        line.location = value
        if (clickable) {
            val chars = PlainTextComponentSerializer.plainText().serialize(obj).toDouble()
            val size = 0.105
            val dist = size * (chars / 2.0)

            hitbox = BoundingBox(-dist, -0.040, -dist, dist, 0.040, dist).shift(value.clone().add(0.0, 2.35, 0.0))
        }
    }

    override fun hide(player: Player) {
        line.destroy(player)
    }

    override fun teleport(player: Player) {
        line.teleport(player)
    }

    override fun show(player: Player) {
        isEmpty = obj.isEmpty()
        if (!isEmpty) {
            line.spawn(player)
            WrapperPlayServerEntityMetadata(
                entityId,
                listOf(
                    EntityData(0, EntityDataTypes.BYTE, 0x20.toByte()),
                    EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(parse())),
                    EntityData(3, EntityDataTypes.BOOLEAN, true)
                )
            ).send(player)
        }
    }

    override fun update(player: Player) {
        val spawnBefore = ((if (isEmpty) 1 else 0) or ((if (obj.isEmpty()) 1 else 0) shl 1))
        /*  0x00  = is already showed
            0x01  = is hided but now has changed
            0x02  = is already showed but is empty
            0x03  = is hided and isn't changed      */
        when (spawnBefore) {
            0x03 -> {}
            0x02 -> {
                line.destroy(player)
                isEmpty = true
            }

            0x01 -> {
                line.spawn(player)
                isEmpty = false
                WrapperPlayServerEntityMetadata(
                    entityId,
                    listOf(
                        EntityData(0, EntityDataTypes.BYTE, 0x20.toByte()),
                        EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(parse())),
                        EntityData(3, EntityDataTypes.BOOLEAN, true)
                    )
                ).send(player)
            }

            0x00 -> {
                WrapperPlayServerEntityMetadata(
                    entityId,
                    listOf(
                        EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(parse())),
                        EntityData(3, EntityDataTypes.BOOLEAN, true)
                    )
                ).send(player)
            }
        }
    }

    private fun Component.isEmpty(): Boolean = PlainTextComponentSerializer.plainText().serialize(this).isEmpty()

}