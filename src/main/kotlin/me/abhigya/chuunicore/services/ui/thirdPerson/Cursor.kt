package me.abhigya.chuunicore.services.ui.thirdPerson

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.model.Observer
import me.abhigya.chuunicore.model.geometry.*
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.hologram.Hologram
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs

class Cursor(
    override val ui: ThirdPersonUI,
    val defaultIcon: ItemStack,
    val selectIcon: ItemStack,
    elementDistanceMultiplier: Double = 0.5
) : TPUIElement {

    private val locationObserver: Observer<Pos3D> = Observer { _, new -> hologram?.teleport(new.toLocation(ui.world)) }
    private val _location = mutableStateOf(host.location.toPos3D() + (ui.frameDirection.toPos3D() * elementDistanceMultiplier) + Pos3D(0.0, 0.5, 0.0))
    override val location: Pos3D get() = _location.get()
    val icon = mutableStateOf(defaultIcon)
    private var hologram: Hologram? = null
    private var task: Job? = null

    override val isRendered: Boolean get() = hologram != null

    override suspend fun render() {
        hologram = Hologram {
            key = UUID.randomUUID().toString()
            location = this@Cursor.location.toLocation(ui.world)

            item(this@Cursor.icon)
        }

        hologram?.show(host)
        _location.addObserver(locationObserver)

        task = coroutineScope {
            launch {
                val originalY = location.y
                val cursorLeftDirection = ui.frameDirection.getCrossProduct(Vector(0, 2, 0))
                val cursorRightDirection = ui.frameDirection.getCrossProduct(Vector(0, -2, 0))

                var previousYaw = host.location.yaw
                var cursorIconState = false
                while (true) {
                    val currentYaw = host.location.yaw
                    val yawDifference = abs(abs(previousYaw) - abs(currentYaw))

                    var newLocation = location.copy(y = originalY + -host.location.pitch * 0.05)

                    val bothPositive = previousYaw >= 0 && currentYaw >= 0
                    val bothNegative = previousYaw < 0 && currentYaw < 0

                    val movementStatus = if ((bothPositive && currentYaw < previousYaw) || (bothNegative && currentYaw < previousYaw)
                        || ((currentYaw > 0 && currentYaw > 100) && previousYaw < 0)) {
                        1
                    } else if ((bothPositive && currentYaw > previousYaw) || (bothNegative && currentYaw > previousYaw)
                        || ((currentYaw < 0 && currentYaw < -100) && (previousYaw > 0))) {
                        2
                    } else {
                        0
                    }

                    previousYaw = currentYaw

                    val moveDirection = when (movementStatus) {
                        1 -> cursorLeftDirection
                        2 -> cursorRightDirection
                        else -> continue
                    }

                    newLocation = newLocation.copy(x = newLocation.x + moveDirection.x * yawDifference * 0.05, z = newLocation.z + moveDirection.z * yawDifference * 0.05)
                    _location.set(newLocation)

//                    val hoveredButtonLocation: Location = checkCursorButtonDistance(false)
//                    if (hoveredButtonLocation != null && this.cursorIconState === 0) {
//                        updateArmorStandHeadItem(cursorEntityID, clickCursorItem)
//                        this.cursorIconState = 1
//                    } else if (hoveredButtonLocation == null && this.cursorIconState === 1) {
//                        updateArmorStandHeadItem(cursorEntityID, defaultCursorItem)
//                        this.cursorIconState = 0
//                    }
                }
            }
        }
    }

//        private fun checkCursorButtonDistance(goForClosest: Boolean = false): Location {
//            var closestButtonLocation: Location
//            var closestDistance = Float.MAX_VALUE.toDouble()
//            for (buttonLocation in this.buttonRegistry.keySet()) {
//                val distanceBetween: Double = this.cursorLocation.distance(buttonLocation)
//                if (distanceBetween < closestDistance && distanceBetween < 0.35) {
//                    closestButtonLocation = buttonLocation
//                    closestDistance = distanceBetween
//                    if (!goForClosest) {
//                        return buttonLocation
//                    }
//                }
//            }
//
//            return closestButtonLocation!!
//        }

    override suspend fun remove() {
        task?.cancelAndJoin()
        task = null
        hologram?.destroy()
        _location.removeObserver(locationObserver)
        hologram = null
    }
}