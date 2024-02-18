package me.abhigya.chuunicore.services.hologram

import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import me.abhigya.chuunicore.model.ClickEvent
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.*

sealed class HologramLine<C>(
    val pool: HologramPool,
    val parent: HologramPage,
    content: C,
    val height: Float
) {

    val entityIds: Array<Int> = arrayOf(SpigotReflectionUtil.generateEntityId(), SpigotReflectionUtil.generateEntityId())
    private val _clickActions: MutableSet<ClickEvent> = Collections.synchronizedSet(HashSet())
    val clickActions: Set<ClickEvent> get() = _clickActions.toSet()

    var content: C = content
        set(value) {
            field = value
            pool.updateContent(parent.parent)
        }

    constructor(pool: HologramPool, parent: HologramPage, content: C, height: Height) : this(pool, parent, content, height.value)

    fun addClickAction(clickAction: ClickEvent) {
        _clickActions.add(clickAction)
    }

    enum class Height(val value: Float) {
        TEXT(0.25F),
        HEAD(2.0F),
        SMALL_HEAD(1.1875F),
        ICON(0.55F),
    }

    class Text(pool: HologramPool, parent: HologramPage, content: Component) : HologramLine<Component>(pool, parent, content, Height.TEXT)

    class Head(pool: HologramPool, parent: HologramPage, content: ItemStack) : HologramLine<ItemStack>(pool, parent, content, Height.HEAD)

    class SmallHead(pool: HologramPool, parent: HologramPage, content: ItemStack) : HologramLine<ItemStack>(pool, parent, content, Height.SMALL_HEAD)

    class Icon(pool: HologramPool, parent: HologramPage, content: ItemStack) : HologramLine<ItemStack>(pool, parent, content, Height.ICON)

    class Entity(pool: HologramPool, parent: HologramPage, entity: HologramEntityType) : HologramLine<HologramEntityType>(pool, parent, entity, entity.height)

}