package me.abhigya.chuunicore.features

import me.abhigya.chuunicore.ChuuniCorePlugin

interface Feature {

    val isEnabled: Boolean

    suspend fun enable(plugin: ChuuniCorePlugin)

    suspend fun disable(plugin: ChuuniCorePlugin)

}