package me.abhigya.chuunicore.model.guardian.skill

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.SkillHolder
import io.lumine.mythic.core.skills.SkillCondition
import java.util.*

object EmptySkill : Skill {
    override fun getInternalName(): String = "null"

    override fun getConfig(): MythicConfig? = null

    override fun getParent(): Optional<SkillHolder> = Optional.empty()

    override fun setParent(parent: SkillHolder?) {
    }

    override fun isInlineSkill(): Boolean = true

    override fun execute(
        trigger: SkillTrigger<*>?,
        caster: SkillCaster?,
        entity: AbstractEntity?,
        location: AbstractLocation?,
        entities: HashSet<AbstractEntity>?,
        locations: HashSet<AbstractLocation>?,
        power: Float
    ) {
    }

    override fun execute(metadata: SkillMetadata?) {
    }

    override fun isUsable(metadata: SkillMetadata?): Boolean = false

    override fun isUsable(metadata: SkillMetadata?, trigger: SkillTrigger<*>?): Boolean = false

    override fun onCooldown(caster: SkillCaster?): Boolean = false

    override fun getConditions(): List<SkillCondition> = emptyList()

    override fun getConditionsTarget(): List<SkillCondition> = emptyList()

    override fun getConditionsTrigger(): List<SkillCondition> = emptyList()
}