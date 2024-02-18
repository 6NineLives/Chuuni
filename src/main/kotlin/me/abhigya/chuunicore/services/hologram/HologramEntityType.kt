package me.abhigya.chuunicore.services.hologram

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes

enum class HologramEntityType(val type: EntityType, val isBaby: Boolean = false) {
    CREEPER(EntityTypes.CREEPER) {
        public override fun height(version: ServerVersion): Float {
            if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) return 1.7f
            return 1.8f
        }
    },
    SKELETON(EntityTypes.SKELETON) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_11_2)) 1.99f else 1.8f
        }
    },
    SPIDER(EntityTypes.SPIDER) {
        public override fun height(version: ServerVersion): Float {
            return 0.9f
        }
    },
    GIANT(EntityTypes.GIANT) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_14_1)) 12f else 10.8f
        }
    },
    ZOMBIE(EntityTypes.ZOMBIE) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    GHAST(EntityTypes.GHAST) {
        public override fun height(version: ServerVersion): Float {
            return 4.0f
        }
    },
    ENDERMAN(EntityTypes.ENDERMAN) {
        public override fun height(version: ServerVersion): Float {
            return 2.9f
        }
    },
    CAVE_SPIDER(EntityTypes.CAVE_SPIDER) {
        public override fun height(version: ServerVersion): Float {
            return 0.5f
        }
    },
    SILVERFISH(EntityTypes.SILVERFISH) {
        public override fun height(version: ServerVersion): Float {
            return 0.3f
        }
    },
    BLAZE(EntityTypes.BLAZE) {
        public override fun height(version: ServerVersion): Float {
            return 1.8f
        }
    },
    WITHER(EntityTypes.WITHER) {
        public override fun height(version: ServerVersion): Float {
            return 3.5f
        }
    },
    BAT(EntityTypes.BAT) {
        public override fun height(version: ServerVersion): Float {
            return 0.9f
        }
    },
    WITCH(EntityTypes.WITCH) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    ENDERMITE(EntityTypes.ENDERMITE) {
        public override fun height(version: ServerVersion): Float {
            return 0.3f
        }
    },
    GUARDIAN(EntityTypes.GUARDIAN) {
        public override fun height(version: ServerVersion): Float {
            return 0.85f
        }
    },
    ELDER_GUARDIAN(EntityTypes.ELDER_GUARDIAN) {
        public override fun height(version: ServerVersion): Float {
            return 2.0f
        }
    },
    PIG(EntityTypes.PIG) {
        public override fun height(version: ServerVersion): Float {
            return 0.9f
        }
    },
    SHEEP(EntityTypes.SHEEP) {
        public override fun height(version: ServerVersion): Float {
            return 1.3f
        }
    },
    COW(EntityTypes.COW) {
        public override fun height(version: ServerVersion): Float {
            return 1.3f
        }
    },
    CHICKEN(EntityTypes.CHICKEN) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    SQUID(EntityTypes.SQUID) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.8f else 0.95f
        }
    },
    WOLF(EntityTypes.WOLF) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.95f else 0.8f
        }
    },
    MUSHROOM_COW(EntityTypes.MOOSHROOM) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 1.4f else 1.3f
        }
    },
    SNOWMAN(EntityTypes.SNOW_GOLEM) {
        public override fun height(version: ServerVersion): Float {
            return 1.9f
        }
    },
    OCELOT(EntityTypes.OCELOT) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    IRON_GOLEM(EntityTypes.IRON_GOLEM) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 2.7f else 2.9f
        }
    },
    HORSE(EntityTypes.HORSE) {
        public override fun height(version: ServerVersion): Float {
            return 1.6f
        }
    },
    RABBIT(EntityTypes.RABBIT) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.5f else 0.7f
        }
    },
    VILLAGER(EntityTypes.VILLAGER) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 1.95f else 1.8f
        }
    },
    POLAR_BEAR(EntityTypes.POLAR_BEAR) {
        public override fun height(version: ServerVersion): Float {
            return 1.4f
        }
    },
    WITHER_SKELETON(EntityTypes.WITHER_SKELETON) {
        public override fun height(version: ServerVersion): Float {
            return 2.4f
        }
    },
    HUSK(EntityTypes.HUSK) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    VINDICATOR(EntityTypes.VINDICATOR) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    EVOKER(EntityTypes.EVOKER) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    LLAMA(EntityTypes.LLAMA) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_13_1)) 1.87f else 1.6f
        }
    },
    ZOMBIE_VILLAGER(EntityTypes.ZOMBIE_VILLAGER) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },

    BABY_PIG(EntityTypes.PIG, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.45f
        }
    },
    BABY_SHEEP(EntityTypes.SHEEP, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.65f
        }
    },
    BABY_COW(EntityTypes.COW, true) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.7f else 0.65f
        }
    },
    BABY_CHICKEN(EntityTypes.CHICKEN, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.35f
        }
    },
    BABY_WOLF(EntityTypes.WOLF, true) {
        public override fun height(version: ServerVersion): Float {
            if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) return 0.4025f //FUCK U MOJANG

            return 0.4f
        }
    },
    BABY_MUSHROOM_COW(EntityTypes.MOOSHROOM, true) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.7f else 0.65f
        }
    },
    BABY_OCELOT(EntityTypes.OCELOT, true) {
        public override fun height(version: ServerVersion): Float {
            return 0f
        }
    },
    BABY_HORSE(EntityTypes.HORSE, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.8f
        }
    },
    BABY_RABBIT(EntityTypes.RABBIT, true) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) 0.25f else 0.35f
        }
    },
    BABY_VILLAGER(EntityTypes.VILLAGER, true) {
        public override fun height(version: ServerVersion): Float {
            if (version.isNewerThanOrEquals(ServerVersion.V_1_9_2)) return 0.9075f //FUCK U MOJANG

            return 0.9f
        }
    },
    BABY_POLAR_BEAR(EntityTypes.POLAR_BEAR, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    DONKEY(EntityTypes.DONKEY) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_14_1)) 1.6f else 1.5f
        }
    },
    BABY_DONKEY(EntityTypes.DONKEY, true) {
        public override fun height(version: ServerVersion): Float {
            return if (version.isNewerThanOrEquals(ServerVersion.V_1_14_1)) 0.8f else 0.795f
        }
    },
    MULE(EntityTypes.MULE) {
        public override fun height(version: ServerVersion): Float {
            return 1.6f
        }
    },
    BABY_MULE(EntityTypes.MULE, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.8f
        }
    },
    SKELETON_HORSE(EntityTypes.SKELETON_HORSE) {
        public override fun height(version: ServerVersion): Float {
            return 1.6f
        }
    },
    BABY_SKELETON_HORSE(EntityTypes.SKELETON_HORSE, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.8f
        }
    },
    VEX(EntityTypes.VEX) {
        public override fun height(version: ServerVersion): Float {
            return 0.8f
        }
    },
    BABY_LLAMA(EntityTypes.LLAMA, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.935f
        }
    },
    TRADER_LLAMA(EntityTypes.TRADER_LLAMA) {
        public override fun height(version: ServerVersion): Float {
            return 1.87f
        }
    },
    BABY_TRADER_LLAMA(EntityTypes.TRADER_LLAMA, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.935f
        }
    },
    ILLUSIONER(EntityTypes.ILLUSIONER) {
        public override fun height(version: ServerVersion): Float {
            return 0f
        }
    },
    PARROT(EntityTypes.PARROT) {
        public override fun height(version: ServerVersion): Float {
            return 0.9f
        }
    },
    BABY_PARROT(EntityTypes.PARROT, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.45f
        }
    },
    DROWNED(EntityTypes.DROWNED) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    BABY_DROWNED(EntityTypes.DROWNED, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.975f
        }
    },
    SALMON(EntityTypes.SALMON) {
        public override fun height(version: ServerVersion): Float {
            return 0.4f
        }
    },
    COD(EntityTypes.COD) {
        public override fun height(version: ServerVersion): Float {
            return 0.3f
        }
    },
    PHANTOM(EntityTypes.PHANTOM) {
        public override fun height(version: ServerVersion): Float {
            return 0.5f
        }
    },
    TURTLE(EntityTypes.TURTLE) {
        public override fun height(version: ServerVersion): Float {
            return 0.4f
        }
    },
    BABY_TURTLE(EntityTypes.TURTLE, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.12f
        }
    },
    TROPICAL_FISH(EntityTypes.TROPICAL_FISH) {
        public override fun height(version: ServerVersion): Float {
            return 0.4f
        }
    },
    PUFFERFISH(EntityTypes.PUFFERFISH) {
        public override fun height(version: ServerVersion): Float {
            return 0.35f
        }
    },
    DOLPHIN(EntityTypes.DOLPHIN) {
        public override fun height(version: ServerVersion): Float {
            return 0.6f
        }
    },
    PANDA(EntityTypes.PANDA) {
        public override fun height(version: ServerVersion): Float {
            return 1.25f
        }
    },
    PANDA_BABY(EntityTypes.PANDA, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.625f
        }
    },
    PILLAGER(EntityTypes.PILLAGER) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    RAVAGER(EntityTypes.RAVAGER) {
        public override fun height(version: ServerVersion): Float {
            return 2.2f
        }
    },
    WANDERING_TRADER(EntityTypes.WANDERING_TRADER) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    FOX(EntityTypes.FOX) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    BABY_FOX(EntityTypes.FOX, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.35f
        }
    },
    CAT(EntityTypes.CAT) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    BABY_CAT(EntityTypes.CAT, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.35f
        }
    },
    STRIDER(EntityTypes.STRIDER) {
        public override fun height(version: ServerVersion): Float {
            return 1.7f
        }
    },
    BABY_STRIDER(EntityTypes.STRIDER, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.85f
        }
    },
    HOGLIN(EntityTypes.HOGLIN) {
        public override fun height(version: ServerVersion): Float {
            return 1.4f
        }
    },
    BABY_HOGLIN(EntityTypes.HOGLIN, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    ZOGLIN(EntityTypes.ZOGLIN) {
        public override fun height(version: ServerVersion): Float {
            return 1.4f
        }
    },
    BABY_ZOGLIN(EntityTypes.ZOGLIN, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.7f
        }
    },
    ZOMBIFIED_PIGLIN(EntityTypes.ZOMBIFIED_PIGLIN) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    PIGLIN(EntityTypes.PIGLIN) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    PIGLIN_BRUTE(EntityTypes.PIGLIN_BRUTE) {
        public override fun height(version: ServerVersion): Float {
            return 1.95f
        }
    },
    BABY_ZOMBIFIED_PIGLIN(EntityTypes.ZOMBIFIED_PIGLIN, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.975f
        }
    },
    BABY_PIGLIN(EntityTypes.PIGLIN, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.975f
        }
    },
    BABY_ZOMBIE_VILLAGER(EntityTypes.ZOMBIE_VILLAGER, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.975f
        }
    },
    GOAT(EntityTypes.GOAT) {
        public override fun height(version: ServerVersion): Float {
            return 1.3f
        }
    },
    BABY_GOAT(EntityTypes.GOAT, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.65f
        }
    },
    AXOLOTL(EntityTypes.AXOLOTL) {
        public override fun height(version: ServerVersion): Float {
            return 0.42f
        }
    },
    BABY_AXOLOTL(EntityTypes.AXOLOTL, true) {
        public override fun height(version: ServerVersion): Float {
            return 0.21f
        }
    },
    GLOW_SQUID(EntityTypes.GLOW_SQUID) {
        public override fun height(version: ServerVersion): Float {
            return 0.8f
        }
    };

    val height: Float by lazy { height(PacketEvents.getAPI().serverManager.version) }

    protected abstract fun height(version: ServerVersion): Float

}