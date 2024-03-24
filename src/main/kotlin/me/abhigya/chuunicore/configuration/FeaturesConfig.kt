package me.abhigya.chuunicore.configuration

import kotlinx.serialization.Serializable

@Serializable
data class FeaturesConfig(
    val speechBubble: Boolean = true,
    val chatChannel: Boolean = true,
) {
}