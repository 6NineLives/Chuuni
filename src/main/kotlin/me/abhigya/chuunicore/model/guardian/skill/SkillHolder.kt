package me.abhigya.chuunicore.model.guardian.skill

import io.lumine.mythic.api.skills.Skill
import kotlinx.serialization.Serializable
import me.abhigya.chuunicore.configuration.SerializableSkill

@Serializable
class SkillHolder(
    val skill1: SerializableSkill,
    val skill2: SerializableSkill,
    val skill3: SerializableSkill,
    val passive: SerializableSkill,
    val ultimate: SerializableSkill
) {
    val all: List<Skill> get() = listOf(skill1, skill2, skill3, passive, ultimate)

}