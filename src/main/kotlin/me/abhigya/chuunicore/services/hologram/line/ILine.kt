package me.abhigya.chuunicore.services.hologram.line

import me.abhigya.chuunicore.services.hologram.Hologram
import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.services.hologram.ClickEvent
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player

interface ILine<T> {

    data class PrivateConfig(private val line: ILine<*>) : Observer {

        lateinit var hologram: Hologram
        override fun observerUpdate() {
            line.update(hologram.seeingPlayers)
        }
    }

    val type: Type

    val entityId: Int

    val location: Location?

    var obj: T

    var pvt : PrivateConfig

    fun setLocation(value: Location)

    fun hide(player: Player)

    fun teleport(player: Player)

    fun show(player: Player)

    fun update(player: Player)

    fun update(seeingPlayers: Iterable<Player>) {
        for (player in seeingPlayers) {
            update(player)
        }
    }

    enum class Type {
        TEXT_LINE,
        BLOCK_LINE,
    }

}