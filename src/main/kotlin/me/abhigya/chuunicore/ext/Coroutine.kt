package me.abhigya.chuunicore.ext

import kotlinx.coroutines.*
import me.abhigya.chuunicore.model.tickable.Tickable
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import toothpick.ktp.delegate.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis

/**
 * A dispatcher that runs tasks on the bukkit main thread.
 */
object BukkitCoroutineDispatcher : CoroutineDispatcher() {

    internal val plugin: Plugin by inject()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }

}

val Dispatchers.Bukkit: CoroutineDispatcher get() = BukkitCoroutineDispatcher

suspend inline fun <T> bukkitContext(noinline block: CoroutineScope.() -> T) = withContext(Dispatchers.Bukkit, block)

inline fun CoroutineScope.scheduleTickTask(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(context, start) {
        while (true) {
            val taken = measureTimeMillis {
                runCatching {
                    block()
                }.onFailure {
                    it.printStackTrace()
                }
            }
            delay((50 - taken).coerceAtLeast(0))
        }
    }
}

fun CoroutineScope.scheduleTickTask(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    tickable: Tickable
): Job {
    return scheduleTickTask(context, start) {
        tickable.tick()
    }
}