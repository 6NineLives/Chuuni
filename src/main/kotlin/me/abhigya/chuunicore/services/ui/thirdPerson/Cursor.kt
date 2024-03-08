package me.abhigya.chuunicore.services.ui.thirdPerson

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import kotlinx.coroutines.*
import me.abhigya.chuunicore.ext.send
import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.model.geometry.*
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.hologram.Head
import me.abhigya.chuunicore.services.hologram.Hologram
import me.abhigya.chuunicore.services.hologram.Page
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs

class Cursor(
    override val ui: ThirdPersonUI,
    val defaultIcon: ItemStack,
    val selectIcon: ItemStack,
    elementDistanceMultiplier: Double = 3.5,
    private val intractableDistance: Float = 0.45F,
    private val mouseAcceleration: Float = 0.05F
) : TPUIElement {

    init {
        require(elementDistanceMultiplier > 0.0) { "Element distance multiplier must be greater than 0" }
    }

    private val locationObserver: Observer<Pos3D> = Observer { _, new -> hologram?.teleport(new.toLocation(ui.world)) }
    private val iconObserver: Observer<ItemStack> = Observer { _, new ->
        val h = hologram ?: return@Observer
        h.hologramPages[0].clearLines()
        h.hologramPages[0].addHeadLine(new)
    }

    private val _location = mutableStateOf(ui.location + (ui.direction.toVector().toPos3D() * elementDistanceMultiplier) - Pos3D(0.0, 2.0, 0.0))
    override val location: Pos3D get() = _location.get()
    val icon = mutableStateOf(defaultIcon)
    private var hologram: Hologram? = null
    private var task: Job? = null

    override val isRendered: Boolean get() = hologram != null

    override suspend fun render() {
        var entity: Int = -1
        hologram = Hologram(UUID.randomUUID().toString(), location.toLocation(ui.world)) {
            Page {
                entity = Head(icon.get()).entityIds[0]
            }
        }.also {
            it.show(host)
            it.changeViewerPage(host, 0)
            it.isClickRegistered = false
            WrapperPlayServerEntityHeadLook(entity, ui.direction.opposite.yaw).send(host)
        }

        _location.addObserver(locationObserver)
        icon.addObserver(iconObserver)

        task = ui.plugin.launch(Dispatchers.IO) {
            val originalY = location.y
            val directionVector = ui.direction.toVector()
            val cursorLeftDirection = directionVector.getCrossProduct(Vector(0, -2, 0))
            val cursorRightDirection = directionVector.getCrossProduct(Vector(0, 2, 0))

            val (positiveMax, negativeMax) = when (ui.direction) {
                ThirdPersonUI.Direction.NORTH, ThirdPersonUI.Direction.SOUTH -> location + Pos3D(ui.dimension.width / 2, ui.dimension.height / 2, 0.0) to
                        location - Pos3D(ui.dimension.width / 2, ui.dimension.height / 2, 0.0)
                ThirdPersonUI.Direction.EAST, ThirdPersonUI.Direction.WEST -> location + Pos3D(0.0, ui.dimension.height / 2, ui.dimension.width / 2) to
                        location - Pos3D(0.0, ui.dimension.height / 2, ui.dimension.width / 2)
            }

            var previousYaw = host.location.yaw
            var cursorIconState = false
            while (isActive) {
                delay(20)
                val currentYaw = host.location.yaw
                val yawDifference = abs(abs(previousYaw) - abs(currentYaw))

                var newLocation = location.copy(y = (originalY + -host.location.pitch * mouseAcceleration).coerceIn(negativeMax.y, positiveMax.y))

                val bothPositive = previousYaw >= 0 && currentYaw >= 0
                val bothNegative = previousYaw < 0 && currentYaw < 0

                val moveDirection = if ((bothPositive && currentYaw < previousYaw) || (bothNegative && currentYaw < previousYaw)
                    || (currentYaw > 0 && currentYaw > 100 && previousYaw < 0)) {
                    cursorLeftDirection
                } else if ((bothPositive && currentYaw > previousYaw) || (bothNegative && currentYaw > previousYaw)
                    || (currentYaw < 0 && currentYaw < -100 && (previousYaw > 0))) {
                    cursorRightDirection
                } else {
                    continue
                }

                previousYaw = currentYaw

                newLocation = newLocation.copy(x = (newLocation.x + moveDirection.x * yawDifference * mouseAcceleration).coerceIn(negativeMax.x, positiveMax.x),
                    z = (newLocation.z + moveDirection.z * yawDifference * mouseAcceleration).coerceIn(negativeMax.z, positiveMax.z))
                _location.set(newLocation)

                val hoveredButtonLocation = checkCursorButtonDistance()
                if (hoveredButtonLocation != null && !cursorIconState) {
                    icon.set(selectIcon)
                    cursorIconState = true
                } else if (hoveredButtonLocation == null && cursorIconState) {
                    icon.set(defaultIcon)
                    cursorIconState = false
                }
            }
        }
    }

    internal fun tryClick() {
        val hoveredButtonLocation = checkCursorButtonDistance(true) ?: return
        hoveredButtonLocation.onClick()
    }

    private fun checkCursorButtonDistance(goForClosest: Boolean = false): TPUIButton? {
        var closestButtonLocation: TPUIButton? = null
        var closestDistance = Float.MAX_VALUE.toDouble()
        for (button in ui.buttons) {
            val distanceBetween = location.toVector().distance(button.location.toVector())
            if (distanceBetween < closestDistance && distanceBetween < intractableDistance) {
                closestButtonLocation = button
                closestDistance = distanceBetween
                if (!goForClosest) {
                    return button
                }
            }
        }

        return closestButtonLocation
    }

    override suspend fun remove() {
        task?.cancelAndJoin()
        task = null
        hologram?.destroy()
        _location.removeObserver(locationObserver)
        icon.removeObserver(iconObserver)
        hologram = null
    }
}