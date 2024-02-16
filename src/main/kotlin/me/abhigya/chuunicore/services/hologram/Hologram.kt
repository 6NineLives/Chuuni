package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.services.hologram.line.ILine
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


data class HologramKey(
    val id: String,
    val pool: HologramPool
)

class Hologram(
    val key: HologramKey,
    location: Location,
    val loader: IHologramLoader
) {

    init {
        key.pool.takeCareOf(key, this)
    }

    var location: Location = location
        private set

    val lines: MutableList<ILine<*>> = CopyOnWriteArrayList() // writes are slow and Iterators are fast and consistent.

    val seeingPlayers: MutableSet<Player> = ConcurrentHashMap.newKeySet() // faster writes

    private var showEvent: ShowEvent? = null
    private var hideEvent : HideEvent? = null

    fun onShow(showEvent: ShowEvent) : Hologram {
        this.showEvent = showEvent
        return this
    }

    fun onHide(hideEvent: HideEvent) : Hologram {
        this.hideEvent = hideEvent
        return this
    }

    fun load(vararg lines: ILine<*>) {
        this.lines.clear()
        lines.forEach { it.pvt.hologram = this }
        loader.load(this, lines)
    }

    fun teleport(to: Location) {
        this.location = to.clone()
        loader.teleport(this)
    }

    fun isShownFor(player: Player): Boolean {
        return seeingPlayers.contains(player)
    }

    fun show(player: Player) {
        seeingPlayers.add(player)
        for (line in lines) {
            line.show(player)
        }

        showEvent?.onShow(player)
    }

    fun showNearby() {
        location.world?.getNearbyPlayers(location, key.pool.options.spawnDistance)?.forEach { show(it) }
    }

    fun hide(player: Player) {
        for (line in lines) {
            line.hide(player)
        }
        seeingPlayers.remove(player)

        hideEvent?.onHide(player)
    }

    fun destroy() {
        key.pool.remove(key)
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val hologram = other as Hologram

        return Objects.equals(key, hologram.key)
    }

    override fun toString(): String {
        return "Hologram[key=${key.id}]"
    }

}