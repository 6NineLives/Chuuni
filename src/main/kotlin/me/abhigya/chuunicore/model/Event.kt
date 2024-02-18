package me.abhigya.chuunicore.model

import org.bukkit.entity.Player

fun interface ClickEvent {

    fun onClick(player: Player, clickType: ClickType)

}

fun interface ShowEvent {

    fun onShow(player: Player)

}

fun interface HideEvent {

    fun onHide(player: Player)

}

enum class ClickType {
    LEFT_CLICK,
    RIGHT_CLICK,
    SHIFT_LEFT_CLICK,
    SHIFT_RIGHT_CLICK
}