package me.abhigya.chuunicore.database

import me.abhigya.chuunicore.configuration.DataBaseSettingsConfig
import java.util.logging.Logger

class DatabaseSettingsValidator(
    private val config: DataBaseSettingsConfig,
    private val logger: Logger
) {

    var effectiveVendor: Vendor = config.vendor
        private set

    fun validate() {
        val username = config.authDetails.username
        val password = config.authDetails.password
        if (config.vendor.isRemote() && username == "default" || password == "default") {
            logger.warning("The username and/or password for the database is not set! Defaulting to local database!")
            effectiveVendor = Vendor.HSQLDB
        }
    }

}