package me.abhigya.chuunicore.model.guardian.rpg

import kotlinx.serialization.Serializable
import me.abhigya.chuunicore.configuration.AttributeSerializer
import me.abhigya.chuunicore.configuration.SerializableComponent
import me.abhigya.chuunicore.model.guardian.skill.SkillHolder

@Serializable
class RPGCharacter(
    val id: String,
    val displayName: SerializableComponent,
    val description: SerializableComponent,
    val element: ElementType,
    @Serializable(AttributeSerializer::class) val attributes: Attribute,
    val skills: SkillHolder
) {

}