package me.abhigya.chuunicore.services.ui

interface UI {

    val isRendered: Boolean

    suspend fun render()

    suspend fun dispose()

}

interface UIElement {

    val ui: UI

    val isRendered: Boolean

    suspend fun render()

    suspend fun remove()

}

interface UIButton : UIElement {

    var onClick: suspend () -> Unit

}