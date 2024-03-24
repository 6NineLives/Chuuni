package me.abhigya.chuunicore.model.guardian.skill

import io.lumine.mythic.api.skills.Skill
import kotlinx.serialization.Serializable
import me.abhigya.chuunicore.configuration.SerializableSkill

@Serializable
class SkillHolder(
    val skill1: SerializableSkill = EmptySkill,
    val skill2: SerializableSkill = EmptySkill,
    val skill3: SerializableSkill = EmptySkill,
    val passive: SerializableSkill = EmptySkill,
    val ultimate: SerializableSkill = EmptySkill
) {

    val all: List<Skill> get() = listOf(skill1, skill2, skill3, passive, ultimate)

}