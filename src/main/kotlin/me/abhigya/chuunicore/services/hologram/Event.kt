package me.abhigya.chuunicore.services.hologram

import org.bukkit.entity.Player

fun interface ClickEvent {

    fun onClick(player: Player)

}

fun interface ShowEvent {

    fun onShow(player: Player)

}

fun interface HideEvent {

    fun onHide(player: Player)

}