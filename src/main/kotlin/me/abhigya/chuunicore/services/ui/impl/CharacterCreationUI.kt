package me.abhigya.chuunicore.services.ui.impl

import me.abhigya.chuunicore.model.geometry.Pos2D
import me.abhigya.chuunicore.model.geometry.Pos3D
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.ui.thirdPerson.Cursor
import me.abhigya.chuunicore.services.ui.thirdPerson.SignButton
import me.abhigya.chuunicore.services.ui.thirdPerson.ThirdPersonUI
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CharacterCreationUI(
    host: Player,
    location: Pos3D,
    direction: Direction,
    override val world: World
) : ThirdPersonUI(host, location, direction, Dimension(3.0, 3.0)) {

    override fun setup() {
        buttons.add(SignButton(
            this,
            mutableStateOf(arrayOf(Component.text("Previous"))),
            relativeLocation = Pos2D(2.0, 2.0),
            onClick = {}
        ))
        cursor = Cursor(
            this,
            ItemStack(Material.ARROW),
            ItemStack(Material.COMPASS)
        )
    }

}