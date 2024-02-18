package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.ChuuniCorePlugin
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import toothpick.ktp.extension.getInstance

fun Hologram(
    key: String,
    location: Location,
    pool: HologramPool = ChuuniCorePlugin.getPlugin().scope.getInstance(),
    init: Hologram.() -> Unit
): Hologram {
    return pool.createHologram(key, location).apply(init)
}

fun HologramPool.create(
    key: String,
    location: Location,
    init: Hologram.() -> Unit
): Hologram {
    return createHologram(key, location).apply(init)
}

fun Hologram.Page(
    init: HologramPage.() -> Unit
) {
    addPage().apply(init)
}

fun HologramPage.Text(
    content: Component
): HologramLine.Text {
    return addTextLine(content)
}

fun HologramPage.Head(
    content: ItemStack
): HologramLine.Head {
    return addHeadLine(content)
}

fun HologramPage.SmallHead(
    content: ItemStack
): HologramLine.SmallHead {
    return addSmallHeadLine(content)
}

fun HologramPage.Icon(
    content: ItemStack
): HologramLine.Icon {
    return addIconLine(content)
}

fun HologramPage.Entity(
    content: HologramEntityType
): HologramLine.Entity {
    return addEntityLine(content)
}

fun HologramPage.Text(
    index: Int,
    content: Component
): HologramLine.Text {
    return insertTextLine(index, content)
}

fun HologramPage.Head(
    index: Int,
    content: ItemStack
): HologramLine.Head {
    return insertHeadLine(index, content)
}

fun HologramPage.SmallHead(
    index: Int,
    content: ItemStack
): HologramLine.SmallHead {
    return insertSmallHeadLine(index, content)
}

fun HologramPage.Icon(
    index: Int,
    content: ItemStack
): HologramLine.Icon {
    return insertIconLine(index, content)
}

fun HologramPage.Entity(
    index: Int,
    content: HologramEntityType
): HologramLine.Entity {
    return insertEntityLine(index, content)
}