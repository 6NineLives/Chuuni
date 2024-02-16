package me.abhigya.chuunicore.services.ui.thirdPerson

import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.geometry.Pos2D
import me.abhigya.chuunicore.model.geometry.toLocation
import me.abhigya.chuunicore.services.hologram.Hologram
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemButton(
    ui: ThirdPersonUI,
    val item: MutableState<ItemStack>,
    relativeLocation: Pos2D,
    elementDistanceMultiplier: Double = 0.52,
    onClick: suspend () -> Unit
) : AbstractTPUIButton(ui, relativeLocation, elementDistanceMultiplier, onClick) {
    override val isRendered: Boolean get() = hologram != null
    private var hologram: Hologram? = null

    override suspend fun render() {
        hologram = Hologram {
            key = UUID.randomUUID().toString()
            location = this@ItemButton.location.toLocation(world)

            item(this@ItemButton.item)
        }

        hologram?.show(host)
    }

    override suspend fun remove() {
        hologram?.hide(host)
        hologram = null
    }
}