package me.abhigya.chuunicore.database.binding

import org.jooq.Converter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DurationConverter : Converter<Long, Duration> {
    override fun from(databaseObject: Long): Duration {
        return databaseObject.milliseconds
    }

    override fun to(userObject: Duration): Long {
        return userObject.inWholeMilliseconds
    }

    override fun fromType(): Class<Long> = Long::class.java

    override fun toType(): Class<Duration> = Duration::class.java

}