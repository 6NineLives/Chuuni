package me.abhigya.chuunicore.model.guardian.rpg

import kotlinx.serialization.Serializable
import me.abhigya.chuunicore.configuration.AttributeSerializer
import me.abhigya.chuunicore.configuration.SerializableComponent
import me.abhigya.chuunicore.model.guardian.skill.SkillHolder

@Serializable
data class RPGClass(
    val id: String,
    val displayName: SerializableComponent,
    val description: SerializableComponent,
    val customModelData: Int,
    val element: ElementType,
    @Serializable(AttributeSerializer::class) val attributes: Attribute,
    val skills: SkillHolder
) {

    fun getAttributeBonusForLevel(attributeType: AttributeType, level: Int): Int {
        val tier = attributes.getInt(attributeType).toFloat()
        val lvl = level.toFloat()

        return ((lvl * ((lvl * tier) / (lvl * 0.2))) + 0.5).toInt()
    }

}

class RPGCharacter(
    val rpgClass: RPGClass,
) {

}