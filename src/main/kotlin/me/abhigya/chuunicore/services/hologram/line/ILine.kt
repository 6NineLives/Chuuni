package me.abhigya.chuunicore.services.hologram.line

import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.services.hologram.Hologram
import org.bukkit.Location
import org.bukkit.entity.Player

interface ILine<T> {

    data class PrivateConfig<T>(private val line: ILine<T>) : Observer<T> {

        lateinit var hologram: Hologram
        override fun observerUpdate(oldValue: T, newValue: T) {
            line.update(hologram.seeingPlayers)
        }
    }

    val type: Type

    val entityId: Int

    val location: Location?

    var obj: T

    var pvt : PrivateConfig<T>

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