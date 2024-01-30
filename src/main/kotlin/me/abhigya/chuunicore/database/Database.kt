package me.abhigya.chuunicore.database

import kotlinx.coroutines.*

abstract class Database(
    val vendor: Vendor
) {

    abstract val isConnected: Boolean

    @Throws(Exception::class)
    abstract suspend fun connect()

    @Throws(Exception::class)
    abstract suspend fun disconnect()

}
