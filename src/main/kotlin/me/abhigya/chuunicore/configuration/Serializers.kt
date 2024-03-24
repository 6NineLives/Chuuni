package me.abhigya.chuunicore.configuration

import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.api.skills.Skill
import io.lumine.mythic.api.skills.SkillManager
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.abhigya.chuunicore.model.geometry.BlockPos
import me.abhigya.chuunicore.model.geometry.Pos2D
import me.abhigya.chuunicore.model.geometry.Pos3D
import me.abhigya.chuunicore.model.guardian.rpg.Attribute
import me.abhigya.chuunicore.model.guardian.rpg.AttributeType
import me.abhigya.chuunicore.model.guardian.skill.EmptySkill
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import toothpick.ktp.delegate.inject
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object HumanReadableDurationSerializer : KSerializer<Duration> {

    private val YEARS = Regex("(?<years>\\d+)(years?|yrs?|y)", RegexOption.IGNORE_CASE)
    private val MONTHS = Regex("(?<months>\\d+)(months?|mo)", RegexOption.IGNORE_CASE)
    private val WEEKS = Regex("(?<weeks>\\d+)(weeks?|w)", RegexOption.IGNORE_CASE)
    private val DAYS = Regex("(?<days>\\d+)(days?|d)", RegexOption.IGNORE_CASE)
    private val HOURS = Regex("(?<hours>\\d+)(hours?|hr|h)", RegexOption.IGNORE_CASE)
    private val MINUTES = Regex("(?<minutes>\\d+)(minutes?|mins?)", RegexOption.IGNORE_CASE)
    private val SECONDS = Regex("(?<seconds>\\d+)(seconds?|sec|s)", RegexOption.IGNORE_CASE)
    private val TICKS = Regex("(?<ticks>\\d+)(ticks?|t)", RegexOption.IGNORE_CASE)
    private val MILLISECONDS = Regex("(?<milliseconds>\\d+)(milliseconds?|ms)", RegexOption.IGNORE_CASE)

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            Duration::class.qualifiedName!!,
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): Duration {
        val time = decoder.decodeString()
        val years = YEARS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val months = MONTHS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val weeks = WEEKS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val days = DAYS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val hours = HOURS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val minutes = MINUTES.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val seconds = SECONDS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val ticks = TICKS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        val millis = MILLISECONDS.findAll(time).sumOf { it.groupValues[1].toLong().coerceAtLeast(0) }
        return (years * 31536000000L +
                months * 2628000000L +
                weeks * 604800000L +
                days * 86400000L +
                hours * 3600000L +
                minutes * 60000L +
                seconds * 1000L +
                ticks * 50L +
                millis).milliseconds
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString("${value.inWholeMilliseconds}ms")
    }

}

typealias HumanReadableDuration = @Serializable(with = HumanReadableDurationSerializer::class) Duration

object ZoneIdSerializer : KSerializer<ZoneId> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(ZoneId::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ZoneId {
        return ZoneId.of(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ZoneId) {
        return encoder.encodeString(value.id)
    }

}

object Pos3DSerializer : KSerializer<Pos3D> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Pos3D::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Pos3D {
        val tokenizer = StringTokenizer(decoder.decodeString(), ":")
        val x = tokenizer.nextToken().toDouble()
        val y = tokenizer.nextToken().toDouble()
        val z = tokenizer.nextToken().toDouble()
        return Pos3D(x, y, z)
    }

    override fun serialize(encoder: Encoder, value: Pos3D) {
        encoder.encodeString(String.format("%.2f:%.2f:%.2f", value.x, value.y, value.z))
    }

}

object Pos2DSerializer : KSerializer<Pos2D> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(Pos2D::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Pos2D {
        val tokenizer = StringTokenizer(decoder.decodeString(), ":")
        val x = tokenizer.nextToken().toDouble()
        val y = tokenizer.nextToken().toDouble()
        return Pos2D(x, y)
    }

    override fun serialize(encoder: Encoder, value: Pos2D) {
        encoder.encodeString(String.format("%.2f:%.2f", value.x, value.y))
    }

}

object BlockPosSerializer : KSerializer<BlockPos> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(BlockPos::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BlockPos {
        val tokenizer = StringTokenizer(decoder.decodeString(), ":")
        val x = tokenizer.nextToken().toDouble().toInt()
        val y = tokenizer.nextToken().toDouble().toInt()
        val z = tokenizer.nextToken().toDouble().toInt()
        return BlockPos(x, y, z)
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeString("${value.x}:${value.y}:${value.z}")
    }

}

object ComponentSerializer : KSerializer<Component> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(Component::class.qualifiedName!!, PrimitiveKind.STRING)

    internal val miniMessage: MiniMessage by inject()

    override fun deserialize(decoder: Decoder): Component {
        return miniMessage.deserialize(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(miniMessage.serialize(value))
    }

}

typealias SerializableComponent = @Serializable(with = ComponentSerializer::class) Component

object AttributeSerializer : KSerializer<Attribute> {

    private val inner: KSerializer<Map<String, Int>> = MapSerializer(String.serializer(), Int.serializer())

    override val descriptor: SerialDescriptor = inner.descriptor

    override fun deserialize(decoder: Decoder): Attribute {
        val map = Attribute()
        inner.deserialize(decoder).forEach { (s, i) ->
            map[AttributeType.valueOf(s.uppercase())] = i
        }
        return map
    }

    override fun serialize(encoder: Encoder, value: Attribute) {
        inner.serialize(encoder, value.mapKeys { it.key.name.lowercase() })
    }

}

object SkillSerializer : KSerializer<Skill> {

    private val skillManager: SkillManager = MythicProvider.get().skillManager

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(Skill::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Skill {
        return skillManager.getSkill(decoder.decodeString()).orElse(EmptySkill)
    }

    override fun serialize(encoder: Encoder, value: Skill) {
        return encoder.encodeString(value.internalName)
    }

}

typealias SerializableSkill = @Serializable(SkillSerializer::class) Skill