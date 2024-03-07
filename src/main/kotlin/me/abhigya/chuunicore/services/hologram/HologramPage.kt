package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.model.ClickEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class HologramPage(
    val pool: HologramPool,
    val page: Int,
    val parent: Hologram
) {

    private val _lines: MutableList<HologramLine<*>> = ArrayList()
    val lines: List<HologramLine<*>> get() = _lines.toList()

    private val _clickActions: MutableSet<ClickEvent> = Collections.synchronizedSet(HashSet())
    val clickActions: Set<ClickEvent> get() = _clickActions.toSet()

    var lineGap: Int = 0

    fun clearLines() {
        pool.respawnHologram(parent) {
            parent.hasChangedContentType = true
            _lines.clear()
        }
    }

    fun addTextLine(content: Component): HologramLine.Text = addLine(HologramLine.Text(pool, this, content))

    fun addHeadLine(content: ItemStack): HologramLine.Head = addLine(HologramLine.Head(pool, this, content))

    fun addSmallHeadLine(content: ItemStack): HologramLine.SmallHead = addLine(HologramLine.SmallHead(pool, this, content))

    fun addIconLine(content: ItemStack): HologramLine.Icon = addLine(HologramLine.Icon(pool, this, content))

    fun addEntityLine(content: HologramEntityType): HologramLine.Entity = addLine(HologramLine.Entity(pool, this, content))

    private fun <C, T : HologramLine<C>> addLine(line: T): T {
        pool.respawnHologram(parent) {
            parent.hasChangedContentType = true
            _lines.add(line)
        }
        return line
    }

    fun removeLine(index: Int) {
        pool.respawnHologram(parent) {
            _lines.removeAt(index)
        }
    }

    private fun <C, T : HologramLine<C>> insertLine(index: Int, line: T): T {
        pool.respawnHologram(parent) {
            parent.hasChangedContentType = true
            _lines.add(index, line)
        }
        return line
    }

    fun insertTextLine(index: Int, content: Component): HologramLine.Text = insertLine(index, HologramLine.Text(pool, this, content))

    fun insertHeadLine(index: Int, content: ItemStack): HologramLine.Head = insertLine(index, HologramLine.Head(pool, this, content))

    fun insertSmallHeadLine(index: Int, content: ItemStack): HologramLine.SmallHead = insertLine(index, HologramLine.SmallHead(pool, this, content))

    fun insertIconLine(index: Int, content: ItemStack): HologramLine.Icon = insertLine(index, HologramLine.Icon(pool, this, content))

    fun insertEntityLine(index: Int, content: HologramEntityType): HologramLine.Entity = insertLine(index, HologramLine.Entity(pool, this, content))

    fun addClickAction(clickAction: ClickEvent) {
        _clickActions.add(clickAction)
    }
}