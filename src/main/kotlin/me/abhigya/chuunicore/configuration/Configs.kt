package me.abhigya.chuunicore.configuration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.bukkit.plugin.java.JavaPlugin
import toothpick.InjectConstructor
import java.nio.file.Path
import javax.inject.Singleton
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories

interface Configs {

    val mainConfig: ConfigProvider<MainConfig>

    val databaseConfig: ConfigProvider<DataBaseSettingsConfig>

    val featuresConfig: ConfigProvider<FeaturesConfig>

}

@Singleton
@InjectConstructor
class SimpleConfigs(
    private val plugin: JavaPlugin,
    private val coroutineScope: CoroutineScope
) : Configs {

    private val dataFolder: Path = plugin.dataFolder.toPath()

    override val databaseConfig: ConfigProvider<DataBaseSettingsConfig> = createProvider(dataFolder.resolve("database.yml"))

    override val mainConfig: ConfigProvider<MainConfig> = createProvider(dataFolder.resolve("config.yml"))

    override val featuresConfig: ConfigProvider<FeaturesConfig> = createProvider(dataFolder.resolve("features.yml"))

    suspend fun reloadConfigs(): ConfigProvider.Result {
        dataFolder.createDirectories()

        return ConfigProvider.Result.combine(listOf(
            coroutineScope.async { mainConfig.loadWithDefaults { MainConfig() } },
            coroutineScope.async { databaseConfig.loadWithDefaults { DataBaseSettingsConfig() } },
            coroutineScope.async { featuresConfig.loadWithDefaults { FeaturesConfig() } }
        ).awaitAll())
    }

    fun saveResource(resourcePath: String, outPath: Path, replace: Boolean = false) {
        val outFile = outPath.toFile()
        if (outFile.exists() && !replace) return
        val resource =
            plugin.getResource(resourcePath) ?: throw IllegalArgumentException("Resource not found: $resourcePath")
        outFile.toPath().createParentDirectories()
        resource.use { input ->
            outFile.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
    }

}