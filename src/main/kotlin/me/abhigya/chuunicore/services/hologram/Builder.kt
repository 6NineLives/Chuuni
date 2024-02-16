package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.hologram.line.BlockLine
import me.abhigya.chuunicore.services.hologram.line.ILine
import me.abhigya.chuunicore.services.hologram.line.TextLine
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import toothpick.ktp.extension.getInstance

fun Hologram(
    pool: HologramPool = ChuuniCorePlugin.getPlugin().scope.getInstance(),
    builder: HologramBuilder.() -> Unit
): Hologram {
    val hologramBuilder = HologramBuilder()
    hologramBuilder.builder()
    return hologramBuilder.build(pool)
}

fun HologramPool.create(
    builder: HologramBuilder.() -> Unit
): Hologram {
    return Hologram(this, builder)
}

class HologramBuilder {

    var key: String? = null
    var location: Location? = null
    var loader: IHologramLoader = TextBlockStandardLoader
    private val lines: MutableList<ILine<*>> = ArrayList()

    fun text(
        text: Component,
        vararg args: Any
    ): TextLine {
        val line = TextLine(text, if (args.isEmpty()) null else args, false)
        lines.add(line)
        return line
    }

    fun clickable(
        text: Component,
        vararg args: Any,
        clickEvent: ClickEvent = ClickEvent { }
    ): TextLine {
        val line = TextLine(text, if (args.isEmpty()) null else args, true)
        line.onClick(clickEvent)
        lines.add(line)
        return line
    }

    fun item(item: MutableState<ItemStack>): BlockLine {
        val line = BlockLine(item)
        lines.add(line)
        return line
    }

    fun item(item: ItemStack): BlockLine {
        return item(mutableStateOf(item))
    }

    internal fun build(pool: HologramPool): Hologram {
        return Hologram(
            HologramKey(requireNotNull(key) { "Key is not set!" }, pool),
            requireNotNull(location) { "Location is not set!" },
            loader
        ).also {
            it.load(*lines.toTypedArray())
        }
    }
}