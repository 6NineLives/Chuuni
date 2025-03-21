package me.abhigya.chuunicore.database.sql.postgresql

import me.abhigya.chuunicore.configuration.DataBaseSettingsConfig
import me.abhigya.chuunicore.database.Vendor
import me.abhigya.chuunicore.database.sql.SQLDatabase

class PostGreSQL(settings: DataBaseSettingsConfig) : SQLDatabase(Vendor.POSTGRESQL, settings) {

    override val props: Map<String, Any> = buildMap {
        // Set default connecting settings
        this["defaultRowFetchSize"] = FETCH_SIZE

        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery#postgresql
        this["socketTimeout"] = SOCKET_TIMEOUT

        putAll(settings.postgresConfig.connectionProperties)
    }

    override val url: String = run {
        val credentials = settings.authDetails
        "jdbc:postgresql://${credentials.host}:${credentials.port}/${credentials.database}"
    }

}
