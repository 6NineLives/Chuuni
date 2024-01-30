package me.abhigya.chuunicore.model.tickable

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

interface Tickable {

    suspend fun tick()

}

abstract class SynchronizedTickable : Tickable {

    private val tickSynchronizer: Semaphore = Semaphore(1)

    override suspend fun tick() = tickSynchronizer.withPermit {
        run()
    }

    protected abstract suspend fun run()

}