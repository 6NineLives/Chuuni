package me.abhigya.chuunicore.model.guardian.rpg

import it.unimi.dsi.fastutil.objects.Object2IntMap
import java.text.DecimalFormat

enum class AttributeType(
    val customName: String,
    val shortName: String,
    val incrementPerPoint: Float,
    val bonusFromLevelReduction: Float,
    val description: String
) {
    ELEMENT_DAMAGE("Bonus Element Damage", "Dmg", 1F, 0.1F, "1 bonus element damage per point"),
    ELEMENT_DEFENSE("Bonus Element Defense", "Def", 1F, 0.2F, "1 bonus element defense per point"),
    MAX_HEALTH("Bonus Max Health", "HP", 10F, 0.1F, "10 bonus max health per point"),
    MAX_MANA("Bonus Max Mana", "MP", 1F, 0.05F, "1 bonus max mana per point"),
    CRITICAL_CHANCE("Bonus Critical Chance", "Crit%", 0.0005F, 0.05F, "0.05% bonus critical chance per point"),
    CRITICAL_DAMAGE("Bonus Critical Damage", "CritDmg%", 0.005F, 0.04F, "0.5% bonus critical damage per point"),
}

private val decimalFormat = DecimalFormat("##.##")

fun AttributeType.getIncrementLore(value: Int): String {
    return when (this) {
        AttributeType.ELEMENT_DAMAGE,
        AttributeType.ELEMENT_DEFENSE,
        AttributeType.MAX_HEALTH,
        AttributeType.MAX_MANA -> (value * incrementPerPoint + 0.5F).toInt().toString()
        AttributeType.CRITICAL_CHANCE,
        AttributeType.CRITICAL_DAMAGE -> decimalFormat.format(value * incrementPerPoint * 100) + "%"
    }
}

typealias Attribute = Object2IntMap<AttributeType>