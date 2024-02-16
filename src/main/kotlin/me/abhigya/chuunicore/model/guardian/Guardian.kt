package me.abhigya.chuunicore.model.guardian

import fr.phoenixdevt.profile.PlayerProfile
import fr.phoenixdevt.profile.ProfileList
import me.abhigya.chuunicore.model.guardian.skill.SkillHolder
import java.util.UUID

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
class Guardian<D : ProfileList<P>, P : PlayerProfile<D>>(
    private val profile: P
) : PlayerProfile<D> by profile {

    val skillHolder: SkillHolder = SkillHolder()
    val friends: MutableList<UUID> = mutableListOf()

}