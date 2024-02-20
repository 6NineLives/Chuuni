package me.abhigya.chuunicore.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import me.abhigya.chuunicore.model.geometry.toPos3D
import me.abhigya.chuunicore.services.ui.impl.CharacterCreationUI
import me.abhigya.chuunicore.services.ui.thirdPerson.ThirdPersonUI
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import toothpick.InjectConstructor

@InjectConstructor
class Test(
    private val coroutineScope: CoroutineScope
) {

    @Command("test")
    fun test(player: Player, direction: ThirdPersonUI.Direction) {
        CharacterCreationUI(player, player.location.toPos3D(), direction, player.world).run {
            runBlocking {
                render()
            }
        }
    }

}