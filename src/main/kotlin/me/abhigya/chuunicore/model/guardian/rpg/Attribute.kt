package me.abhigya.chuunicore.model.guardian.rpg

import java.text.DecimalFormat

class Attribute(
    val type: AttributeType
) {

    var bonusFromHelmet: Int = 0
        private set
    var bonusFromChestplate: Int = 0
        private set
    var bonusFromLeggings: Int = 0
        private set
    var bonusFromBoots: Int = 0
        private set
    var bonusFromMainHand: Int = 0
        private set
    var bonusFromOffHand: Int = 0
        private set
    var bonusFromPassive: Int = 0
        private set

    val totalBonus: Int get() = bonusFromHelmet + bonusFromChestplate + bonusFromLeggings + bonusFromBoots + bonusFromMainHand + bonusFromOffHand + bonusFromPassive

}

enum class AttributeType(
    val customName: String,
    val shortName: String,
    val incrementPerPoint: Float,
    val bonusFromLevelReduction: Float,
    val description: String
) {
    BONUS_ELEMENT_DAMAGE("Bonus Element Damage", "Dmg", 1F, 0.1F, "1 bonus element damage per point"),
    BONUS_ELEMENT_DEFENSE("Bonus Element Defense", "Def", 1F, 0.2F, "1 bonus element defense per point"),
    BONUS_MAX_HEALTH("Bonus Max Health", "HP", 10F, 0.1F, "10 bonus max health per point"),
    BONUS_MAX_MANA("Bonus Max Mana", "MP", 1F, 0.05F, "1 bonus max mana per point"),
    BONUS_CRITICAL_CHANCE("Bonus Critical Chance", "Crit%", 0.0005F, 0.05F, "0.05% bonus critical chance per point"),
    BONUS_CRITICAL_DAMAGE("Bonus Critical Damage", "CritDmg%", 0.005F, 0.04F, "0.5% bonus critical damage per point"),
}

private val decimalFormat = DecimalFormat("##.##")

fun AttributeType.getIncrementLore(value: Int): String {
    return when (this) {
        AttributeType.BONUS_ELEMENT_DAMAGE,
        AttributeType.BONUS_ELEMENT_DEFENSE,
        AttributeType.BONUS_MAX_HEALTH,
        AttributeType.BONUS_MAX_MANA -> (value * incrementPerPoint + 0.5F).toInt().toString()
        AttributeType.BONUS_CRITICAL_CHANCE -> decimalFormat.format(value * incrementPerPoint * 100) + "%"
        AttributeType.BONUS_CRITICAL_DAMAGE -> decimalFormat.format(value * incrementPerPoint * 100) + "%"
    }
}