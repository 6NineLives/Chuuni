package me.abhigya.chuunicore.services.hologram

import me.abhigya.chuunicore.services.hologram.line.ILine
import me.abhigya.chuunicore.services.hologram.line.TextLine
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.math.abs

interface IHologramLoader {

    fun load(hologram: Hologram, lines: Array<out ILine<*>>)

    fun teleport(hologram: Hologram)

}

object SingletonLoader : IHologramLoader {
    override fun load(hologram: Hologram, lines: Array<out ILine<*>>) {
        if (lines.size > 1) {
            throw RuntimeException("Hologram '${hologram.key}' has more than 1 line.")
        }

        val cloned = hologram.location.clone()

        val line: ILine<*> = lines[0]

        line.setLocation(cloned)
        hologram.lines.add(line)
    }

    override fun teleport(hologram: Hologram) {
        val line: ILine<*> = hologram.lines[0]

        line.setLocation(hologram.location.clone())
        hologram.seeingPlayers.forEach(line::teleport)
    }
}

object TextBlockStandardLoader : IHologramLoader {
    override fun load(hologram: Hologram, lines: Array<out ILine<*>>) {
        val cloned = hologram.location.clone()

        if (lines.size == 1) {
            val line: ILine<*> = lines[0]

            line.setLocation(cloned)
            hologram.lines.add(line)
            return
        }

        // reverse A - B - C to C - B - A
        lines.reverse()

        cloned.subtract(0.0, 0.28, 0.0)

        for (j in lines.indices) {
            val line: ILine<*> = lines[j]
            var up = 0.28

            if (j > 0) {
                val before: ILine.Type = lines[j - 1].type
                if (before == ILine.Type.BLOCK_LINE) {
                    up = -1.5
                }
            }

            when (line.type) {
                ILine.Type.TEXT_LINE -> {
                    line.setLocation(cloned.add(0.0, up, 0.0).clone())
                    hologram.lines.add(0, line)
                }

                ILine.Type.BLOCK_LINE -> {
                    line.setLocation(cloned.add(0.0, 0.6, 0.0).clone())
                    hologram.lines.add(0, line)
                }
            }
        }
    }

    override fun teleport(hologram: Hologram) {
        val lines: List<ILine<*>> = hologram.lines
        val firstLine: ILine<*> = lines[0]
        // Obtain the Y position of the first line and then calculate the distance to all lines to maintain this distance
        val baseY: Double = firstLine.location?.y ?: throw RuntimeException("First line has not a location")
        // Get position Y where to teleport the first line
        var destY = (hologram.location.y - 0.28)

        destY += when (firstLine.type) {
            ILine.Type.TEXT_LINE -> 0.28
            else -> 0.6
        }

        // Teleport the first line
        this.teleportLine(hologram, destY, firstLine)
        var tempLine: ILine<*>
        for (j in 1 until lines.size) {
            tempLine = lines[j]
            /*
        Teleport from the second line onwards.
        The final height is found by adding to that of the first line the difference that was present when it was already spawned
        */
            this.teleportLine(
                hologram, destY + abs(
                    baseY -
                            (tempLine.location?.y ?: throw RuntimeException("Missing location of line $tempLine"))
                ), tempLine
            )
        }
    }

    private fun teleportLine(hologram: Hologram, destY: Double, tempLine: ILine<*>) {
        val dest = hologram.location.clone()
        dest.y = destY
        tempLine.setLocation(dest)
        hologram.seeingPlayers.forEach(tempLine::teleport)
    }
}

object TextSequentialLoader : IHologramLoader {
    override fun load(hologram: Hologram, lines: Array<out ILine<*>>) {
        set(hologram, lines, true)
    }

    override fun teleport(hologram: Hologram) {
        set(hologram, hologram.lines.toTypedArray(), false)
        // TODO: When teleporting, the holograms unexpectedly become distant. Understand why.
    }

    private fun set(hologram: Hologram, lines: Array<out ILine<*>>, add: Boolean) {
        val cloned = hologram.location.clone()
        for (line in lines) {
            when (line.type) {
                ILine.Type.TEXT_LINE -> {
                    val tL = line as TextLine

                    // add to lines
                    tL.setLocation(cloned.clone())

                    if (add) {
                        hologram.lines.add(0, tL)
                    } else {
                        hologram.seeingPlayers.forEach { tL.teleport(it) }
                    }
                    cloned.z += 0.175 * PlainTextComponentSerializer.plainText().serialize(tL.obj).length
                }

                else -> throw RuntimeException("This method load supports only TextLine & TextALine & ClickableTextLine.")
            }
        }
    }
}
