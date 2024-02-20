package me.abhigya.chuunicore

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import me.abhigya.chuunicore.commands.Commands
import me.abhigya.chuunicore.configuration.ConfigProvider
import me.abhigya.chuunicore.configuration.Configs
import me.abhigya.chuunicore.configuration.SimpleConfigs
import me.abhigya.chuunicore.database.Database
import me.abhigya.chuunicore.database.DatabaseSettingsValidator
import me.abhigya.chuunicore.database.FlywayMigration
import me.abhigya.chuunicore.database.Vendor
import me.abhigya.chuunicore.database.sql.SQLDatabase
import me.abhigya.chuunicore.database.sql.hsqldb.HSQLDB
import me.abhigya.chuunicore.database.sql.mariadb.MariaDB
import me.abhigya.chuunicore.database.sql.mariadb.MySQL
import me.abhigya.chuunicore.database.sql.postgresql.PostGreSQL
import me.abhigya.chuunicore.ext.BukkitCoroutineDispatcher
import me.abhigya.chuunicore.services.hologram.HologramPool
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import toothpick.Scope
import toothpick.configuration.Configuration
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

class ChuuniCorePlugin : JavaPlugin(), CoroutineScope by CoroutineScope(
    SupervisorJob() + CoroutineName("ChunniCore")
) {

    companion object {
        val ROOTSCOPE: Scope = KTP.openRootScope()

        fun getPlugin(): ChuuniCorePlugin {
            return ROOTSCOPE.getInstance()
        }
    }

    val scope: Scope
    var state: PluginState = PluginState.LOADING
        private set

    init {
        KTP.setConfiguration(acquireConfiguration())
        ROOTSCOPE
            .installModules(
                module {
                    bind<ChuuniCorePlugin>().toInstance(this@ChuuniCorePlugin)
                }
            )

        scope = KTP.openScope(this@ChuuniCorePlugin)
    }

    override fun onEnable() {
        state = PluginState.STARTING
        if (isRunningDevelopment) {
            logger.info("Running in development mode!")
            logger.level = Level.ALL
        } else {
            Logger.getLogger("org.flywaydb.core.internal.license.VersionPrinter").level = Level.OFF
        }

        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")

        scope.installModules(
            module {
                bind<Plugin>().toInstance(this@ChuuniCorePlugin)
                bind<JavaPlugin>().toInstance(this@ChuuniCorePlugin)
                bind<ChuuniCorePlugin>().toInstance(this@ChuuniCorePlugin)
                bind<CoroutineScope>().toInstance(this@ChuuniCorePlugin)
                bind<Logger>().toProviderInstance { this@ChuuniCorePlugin.logger }
                bind<MiniMessage>().toProviderInstance {
                    MiniMessage.builder().build()
                }.providesSingleton().providesReleasable()
                bind<Configs>().toClass<SimpleConfigs>().singleton()
                bind<HologramPool>().toInstance { HologramPool(this@ChuuniCorePlugin).also { it.init() } }
            }
        )

        scope.inject(BukkitCoroutineDispatcher)

        // Configuration
        logger.info("Loading configuration...")
        val configs = scope.getInstance<Configs>() as SimpleConfigs
        measureTimeMillis {
            runCatching {
                runBlocking {
                    val result = configs.reloadConfigs()
                    when (result) {
                        ConfigProvider.Result.IO_ERROR -> throw IOException("An error occurred while loading configuration files!")
                        ConfigProvider.Result.INVALID_FORMAT -> throw IOException("An error occurred while parsing configuration files!")
                        else -> {}
                    }
                }
            }.onFailure {
                logger.severe("Failed to load configuration! Check your configuration files!")
                state = PluginState.DISABLED
                throw it
            }
        }.run {
            logger.info("Configuration loaded in $this ms!")
        }

        // Database
        logger.info("Initializing database...")
        measureTimeMillis {
            val dbConf = configs.databaseConfig.get()
            val validator = DatabaseSettingsValidator(dbConf, logger)
            validator.validate()

            val database = when (validator.effectiveVendor) {
                Vendor.HSQLDB -> HSQLDB(dataFolder.toPath().resolve(".data/database/database"), dbConf)
                Vendor.MARIADB -> MariaDB(dbConf)
                Vendor.MYSQL -> MySQL(dbConf)
                Vendor.POSTGRESQL -> PostGreSQL(dbConf)
            }

            runCatching {
                runBlocking { database.connect() }
                FlywayMigration(validator.effectiveVendor, database.dataSource!!).migrate()
            }.onFailure {
                logger.severe("Failed to connect to database! Check your database configuration and the database server!")
                state = PluginState.DISABLED
                throw it
            }

            scope.installModules(
                module {
                    bind<Database>().toInstance(database)
                    bind<SQLDatabase>().toInstance(database)
                }
            )
        }.run {
            logger.info("Database initialized in $this ms!")
        }

        scope.getInstance<Commands>().registerCommands()

        state = PluginState.ENABLED
    }
}

val isRunningDevelopment: Boolean = System.getProperty("environment") == "development"

fun acquireConfiguration(): Configuration {
    return if (isRunningDevelopment ||
        System.getProperty("toothpick.configuration") == "development"
    ) {
        Configuration.forDevelopment()
    } else {
        Configuration.forProduction()
    }
}

enum class PluginState {
    LOADING,
    STARTING,
    ENABLED,
    STOPPING,
    DISABLED
}

fun Logger.debug(message: String) {
    if (isRunningDevelopment) {
        this.info("[DEBUG] $message")
    }
}