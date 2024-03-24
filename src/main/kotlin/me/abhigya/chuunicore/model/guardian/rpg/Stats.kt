package me.abhigya.chuunicore.model.guardian.rpg

const val MAX_HEALTH: Int = 100
const val MAX_MANA: Int = 100
const val BASE_CRITICAL_CHANCE: Float = 0.01F
const val BASE_CRITICAL_DAMAGE: Float = 1.4F

data class Stats(
    var exp: Int = 0,
    var mana: Int = MAX_MANA,
    var helmetStat: ArmorStat = ArmorStat(),
    var chestPlateStat: ArmorStat = ArmorStat(),
    var leggingsStat: ArmorStat = ArmorStat(),
    var bootsStat: ArmorStat = ArmorStat(),
    var shieldStat: ArmorStat = ArmorStat(),
    var offHandDamageBonus: Int = 0,
)

data class ArmorStat(
    val health: Int = 0,
    val defence: Int = 0
)