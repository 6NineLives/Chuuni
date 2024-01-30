package me.abhigya.chuunicore.configuration

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.abhigya.chuunicore.database.Vendor
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
data class DataBaseSettingsConfig(
    @SerialName("rdbms-vendor")
    @YamlComment(
        "The RDBMS vendor to use",
        "Supported vendors | Min. version | Description",
        "HSQLDB            | 2.7          | Local database, selected by default",
        "MySQL             | 8.0          | Remote database",
        "MARIADB           | 10.6         | Remote database",
        "POSTGRESQL        | 12           | Remote database"
    )
    val vendor: Vendor = Vendor.HSQLDB,

    @YamlComment("The maximum number of connections in the pool")
    val connectionPoolSize: Int = 6,

    @YamlComment("The maximum number of seconds to wait for a connection to become available")
    val connectionTimeout: HumanReadableDuration = 30.seconds,

    @YamlComment(
        "The maximum lifetime of a connection in the pool.",
        "This value should be set for MariaDB or MySQL.",
        "HikariCP notes: It should be several seconds shorter than any database or infrastructure imposed connection time limit",
        "Default: 25 min"
    )
    val maxLifetime: HumanReadableDuration = 25.minutes,

    @YamlComment(
        "The authentication details for the database.",
        "Only applicable for remote databases"
    )
    val authDetails: AuthDetails = AuthDetails(),

    @YamlComment("Only applicable for MariaDB")
    val mariadbConfig: MariaDbConfig = MariaDbConfig(),

    @YamlComment("Only applicable for PostgreSQL")
    val postgresConfig: PostgresConfig = PostgresConfig()
)

@Serializable
data class AuthDetails(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "glide",
    val username: String = "default",
    val password: String = "default"
)

@Serializable
data class MariaDbConfig(
    @YamlComment("The connection properties for the database.")
    val connectionProperties: Map<String, String> = mapOf(
        "useUnicode" to "true",
        "characterEncoding" to "UTF-8",
        "useServerPrepStmts" to "true",
        "cachePrepStmts" to "true",
        "prepStmtCacheSize" to "25",
        "prepStmtCacheSqlLimit" to "1024"
    )
)

@Serializable
data class PostgresConfig(
    val connectionProperties: Map<String, String> = mapOf(
        "preparedStatementCacheQueries" to "25"
    )
)