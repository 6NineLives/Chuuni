package me.abhigya.chuunicore.configuration

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.abhigya.chuunicore.isRunningDevelopment
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.reflect.KClass

class ConfigProvider<T : Any>(
    private val confClass: KClass<T>,
    val path: Path
) {

    companion object {
        private val yaml: Yaml = Yaml(
            configuration = YamlConfiguration(
                encodeDefaults = true,
                strictMode = false,
                polymorphismStyle = PolymorphismStyle.Property,
                yamlNamingStrategy = YamlNamingStrategy.KebabCase
            )
        )
    }

    var config: Config<T> = UnInitializedConfig(this@ConfigProvider)
        private set

    fun provideEmpty() {
        config = EmptyConfiguration(this@ConfigProvider)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadConfig(): Result {
        return runCatching {
            if (!path.exists()) {
                provideEmpty()
                return@runCatching Result.MISSING
            }
            path.inputStream(StandardOpenOption.READ).buffered().use {
                config = LoadedConfig(
                    yaml.decodeFromStream(
                        yaml.serializersModule.serializer(confClass, emptyList(), false) as KSerializer<T>,
                        it
                    ), this@ConfigProvider
                )
            }
            Result.SUCCESS
        }.getOrElse {
            if (me.abhigya.chuunicore.isRunningDevelopment) {
                it.printStackTrace()
            }
            Result.IO_ERROR
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun saveConfig(config: T): Result {
        return runCatching {
            path.createParentDirectories()

            path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).buffered().use {
                yaml.encodeToStream(
                    yaml.serializersModule.serializer(confClass, emptyList(), false) as KSerializer<T>,
                    config,
                    it
                )
            }

            this@ConfigProvider.config = LoadedConfig(config, this@ConfigProvider)

            Result.SUCCESS
        }.getOrElse {
            if (me.abhigya.chuunicore.isRunningDevelopment) {
                it.printStackTrace()
            }
            Result.IO_ERROR
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadWithDefaults(lazyDefaults: () -> T): Result {
        return runCatching {
            path.createParentDirectories()

            if (!path.exists()) {
                val defaults = lazyDefaults()
                path.outputStream(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE).buffered().use {
                    yaml.encodeToStream(
                        yaml.serializersModule.serializer(confClass, emptyList(), false) as KSerializer<T>,
                        defaults,
                        it
                    )
                }
                config = LoadedConfig(defaults, this@ConfigProvider)
                return@runCatching Result.SUCCESS_WITH_DEFAULTS
            }

            if (loadConfig() == Result.IO_ERROR) {
                return@runCatching Result.IO_ERROR
            }

            path.outputStream(StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).buffered().use {
                yaml.encodeToStream(
                    yaml.serializersModule.serializer(confClass, emptyList(), false) as KSerializer<T>,
                    config.get(),
                    it
                )
            }

            Result.SUCCESS
        }.getOrElse {
            if (me.abhigya.chuunicore.isRunningDevelopment) {
                it.printStackTrace()
            }
            Result.IO_ERROR
        }
    }

    // ***********************************
    // *        Delegated Members        *
    // ***********************************
    val isPresent: Boolean get() = config.isPresent

    val isLoaded: Boolean get() = config.isLoaded

    fun get(): T {
        return config.get()
    }

    inline fun <R> get(block: T.() -> R): R {
        return config.get(block)
    }

    inline fun ifPresent(consumer: (T) -> Unit) {
        config.ifPresent(consumer)
    }


    enum class Result {
        SUCCESS_WITH_DEFAULTS,
        SUCCESS,
        MISSING,
        INVALID_FORMAT,
        IO_ERROR;

        companion object {
            fun combine(vararg results: Result): Result {
                return combine(results.toList())
            }

            fun combine(results: Collection<Result>): Result {
                return entries[results.maxOfOrNull { it.ordinal } ?: 0]
            }
        }

        val isSuccess: Boolean get() = this == SUCCESS || this == SUCCESS_WITH_DEFAULTS
    }

}

internal inline fun <reified T : Any> createProvider(path: Path): ConfigProvider<T> {
    return ConfigProvider(T::class, path)
}

class MultiConfigProvider<T : Any>(
    val confClass: KClass<T>,
    val configsDir: Path,
    val keyMapper: (Path, T) -> String
) {

    private val providers: MutableMap<String, ConfigProvider<T>> = mutableMapOf()

    operator fun get(key: String): ConfigProvider<T>? = providers[key]

    fun createNew(key: String, fileName: String, defaults: () -> T): ConfigProvider<T> {
        if (key in providers) {
            throw IllegalArgumentException("Config provider already exists!")
        }
        val path = configsDir.resolve("$fileName.yml")
        if (path.exists()) {
            throw IllegalArgumentException("Config file already exists!")
        }

        val provider = ConfigProvider(confClass, path)
        provider.loadWithDefaults(defaults)
        providers[keyMapper(path, provider.config.get())] = provider
        return provider
    }

    fun loadConfig(): ConfigProvider.Result {
        return ConfigProvider.Result.combine(configsDir.listDirectoryEntries("*.yml").map {
            val provider = ConfigProvider(confClass, it)
            val res = provider.loadConfig()
            providers[keyMapper(it, provider.config.get())] = provider
            res
        })
    }

    suspend fun loadConfigParallel(): ConfigProvider.Result = coroutineScope {
        return@coroutineScope ConfigProvider.Result.combine(configsDir.listDirectoryEntries("*.yml").map {
            async {
                val provider = ConfigProvider(confClass, it)
                val res = provider.loadConfig()
                providers[keyMapper(it, provider.config.get())] = provider
                res
            }
        }.awaitAll())
    }

    fun loadWithDefaults(defaults: (Path) -> T): ConfigProvider.Result {
        return ConfigProvider.Result.combine(configsDir.listDirectoryEntries("*.yml").map {
            val provider = ConfigProvider(confClass, it)
            val res = provider.loadWithDefaults { defaults(it) }
            val key = keyMapper(it, provider.config.get())
            providers[key] = provider
            res
        })
    }

    suspend fun loadWithDefaultsParallel(defaults: (Path, String) -> T): ConfigProvider.Result = coroutineScope {
        return@coroutineScope ConfigProvider.Result.combine(configsDir.listDirectoryEntries("*.yml").map {
            async {
                val provider = ConfigProvider(confClass, it)
                val key = keyMapper(it, provider.config.get())
                providers[key] = provider
                provider.loadWithDefaults { defaults(it, key) }
            }
        }.awaitAll())
    }

    fun keys(): Set<String> {
        return providers.keys
    }

    fun all(): Collection<ConfigProvider<T>> {
        return providers.values.toList()
    }

    fun clearAll() {
        providers.clear()
    }

}

internal inline fun <reified T : Any> createMultiProvider(
    configsDir: Path,
    noinline keyMapper: (Path, T) -> String
): MultiConfigProvider<T> {
    return MultiConfigProvider(T::class, configsDir, keyMapper)
}

open class Config<T : Any> internal constructor(
    private val value: T?,
    private val provider: ConfigProvider<T>
) {

    open val isPresent: Boolean get() = value != null

    open val isLoaded: Boolean get() = isPresent

    open fun get(): T {
        return value ?: throw InvalidConfigurationException("Config value is invalid!")
    }

    fun provider(): ConfigProvider<T> {
        return provider
    }

    inline fun <R> get(block: T.() -> R): R {
        return get().block()
    }

    inline fun ifPresent(consumer: (T) -> Unit) {
        if (isPresent) {
            consumer(get())
        }
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(value=$value)"
    }

}

class LoadedConfig<T : Any> internal constructor(config: T, provider: ConfigProvider<T>) : Config<T>(config, provider) {

    override val isPresent: Boolean get() = true

    override val isLoaded: Boolean get() = true

}

class UnInitializedConfig<T : Any> internal constructor(provider: ConfigProvider<T>) : Config<T>(null, provider) {

    override val isPresent: Boolean get() = false

    override val isLoaded: Boolean get() = false

    override fun get(): T {
        throw UninitializedConfigurationException("Config value is not initialized!")
    }

}

class EmptyConfiguration<T : Any> internal constructor(provider: ConfigProvider<T>) : Config<T>(null, provider) {

    override val isPresent: Boolean get() = false

    override val isLoaded: Boolean get() = true

    override fun get(): T {
        throw ConfigNotFoundException("Config value is empty!")
    }

}