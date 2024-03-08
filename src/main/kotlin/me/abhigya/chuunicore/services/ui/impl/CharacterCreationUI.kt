package me.abhigya.chuunicore.services.ui.impl

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import kotlinx.coroutines.launch
import me.abhigya.chuunicore.model.geometry.Pos2D
import me.abhigya.chuunicore.model.geometry.Pos3D
import me.abhigya.chuunicore.model.geometry.toLocation
import me.abhigya.chuunicore.model.mutableStateOf
import me.abhigya.chuunicore.services.ui.thirdPerson.Cursor
import me.abhigya.chuunicore.services.ui.thirdPerson.ItemButton
import me.abhigya.chuunicore.services.ui.thirdPerson.ThirdPersonUI
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class CharacterCreationUI(
    host: Player,
    location: Pos3D,
    direction: Direction,
    private val npcLocation: Pos3D,
    override val world: World
) : ThirdPersonUI(host, location, direction, Dimension(5.0, 5.0)) {

    private val textures = listOf(
        "efc80c87-77e6-47f9-a498-be4be48ce361" to "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhYjQ5N2E1ZDk0NjIxNzVjNGFkN2FkODU1ZDc3ZDc3YmE0OWYzMGQzMmY2ZTU4YzEwMmQwNjJjMzNlYjU5MCJ9fX0=", // mage,
        "ddc9ef02-0c6c-47ea-8cda-bc5b94499633" to "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThhMWQyNjBiODFlYWQxMGI2ZWE5NzRkMDIxZGZhODA3NTIyNDU2YThiYjRiNTU5ZTFmYWNhOTdlZjVmODFmNSJ9fX0=", //warrior
        "bf1759bd-9d5e-423f-8b74-29b1194c788b" to "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2MyMzAzYjVkNGY4Y2YzZTBjMjRjM2RjYjY1MTQ0ODYxNjVjYWUxNDU5ZWU4NzgxZTZkMDIzNTdlM2IwYmVjMyJ9fX0=", //archer
    )
    private var selected: Int = 0

    private var npc: NPC? = null

    override fun setup() = builderScope {
        Cursor(
            ItemStack(Material.ARROW),
            ItemStack(Material.COMPASS)
        )

        createNpc()

        ItemButton(
            mutableStateOf(ItemStack(Material.ANVIL)),
            Pos2D(2.0, 0.0)
        ) {
            selected = if (selected + 1 >= textures.size) 0 else selected + 1
            npc?.despawnAll()
            createNpc()
            host.sendMessage("Changed skin")
        }

        ItemButton(
            mutableStateOf(ItemStack(Material.BARRIER)),
            Pos2D(0.0, -2.5)
        ) {
            host.sendMessage("Closed TPUI")
            plugin.launch { dispose() }
        }
    }

    override suspend fun dispose() {
        super.dispose()

        npc?.despawnAll()
    }

    private fun createNpc() {
        npc = NPC(
            UserProfile(
                UUID.fromString(textures[selected].first),
                "CharacterCreationNPC",
                listOf(
                    TextureProperty(
                        "textures",
                        textures[selected].second,
                        null
                    )
                )
            ),
            SpigotReflectionUtil.generateEntityId(),
            GameMode.CREATIVE,
            null,
            null,
            null,
            null
        ).also {
            it.location = SpigotConversionUtil.fromBukkitLocation(npcLocation.toLocation().setDirection(direction.opposite.toVector()))
            it.spawn(PacketEvents.getAPI().playerManager.getChannel(host))
        }
    }
}