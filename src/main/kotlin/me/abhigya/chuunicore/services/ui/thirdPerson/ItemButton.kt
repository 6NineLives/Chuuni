package me.abhigya.chuunicore.services.ui.thirdPerson

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.MutableState
import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.model.geometry.*
import me.abhigya.chuunicore.services.hologram.Head
import me.abhigya.chuunicore.services.hologram.Hologram
import me.abhigya.chuunicore.services.hologram.Page
import org.bukkit.inventory.ItemStack
import java.util.*

class ItemButton(
    ui: ThirdPersonUI,
    val item: MutableState<ItemStack>,
    relativeLocation: Pos2D,
    elementDistanceMultiplier: Double = 3.52,
    onClick: () -> Unit
) : AbstractTPUIButton(ui, relativeLocation, elementDistanceMultiplier, onClick) {

    private val iconObserver: Observer<ItemStack> = Observer { _, new ->
        val h = hologram ?: return@Observer
        h.hologramPages[0].clearLines()
        h.hologramPages[0].addHeadLine(new)
    }
    override val isRendered: Boolean get() = hologram != null
    private var hologram: Hologram? = null

    override suspend fun render() {
        _location.set(location - Pos3D(0.0, 2.0, 0.0))
        hologram = Hologram(UUID.randomUUID().toString(), location.toLocation(world)) {
            Page {
                Head(item.get())
            }
        }.also {
            it.show(host)
            it.changeViewerPage(host, 0)
            it.isClickRegistered = false
            WrapperPlayServerEntityHeadLook(it.hologramPages[0].lines[0].entityIds[0], ui.direction.opposite.yaw).send(host)
        }

        item.addObserver(iconObserver)
    }

    override suspend fun remove() {
        hologram?.hide(host)
        hologram = null
        item.removeObserver(iconObserver)
    }
}