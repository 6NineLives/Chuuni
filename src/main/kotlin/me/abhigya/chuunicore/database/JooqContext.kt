package me.abhigya.chuunicore.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import me.abhigya.chuunicore.ChuuniCorePlugin
import me.abhigya.chuunicore.database.sql.SQLDatabase
import org.jetbrains.annotations.Blocking
import org.jooq.ConnectionProvider
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.MappedTable
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.NoConnectionProvider
import toothpick.ktp.extension.getInstance
import java.sql.Connection
import kotlin.coroutines.CoroutineContext

class JooqContext(private val dialect: SQLDialect) {

    companion object {
        val MATCH_ALL_EXCEPT_INFORMATION_SCHEMA = Regex("^(?!INFORMATION_SCHEMA)(.*?)$")
        val MATCH_ALL = Regex("^(.*?)\$")
        const val REPLACEMENT = "glide_$0"
    }

    fun createContext(connection: Connection): DSLContext {
        class ConnectionProviderImpl : ConnectionProvider {
            override fun acquire(): Connection {
                return connection
            }

            override fun release(connection: Connection) {
                // do nothing
            }
        }

        return createWith(ConnectionProviderImpl())
    }

    fun createDummyContext(): DSLContext {
        return createWith(NoConnectionProvider())
    }

    private fun createWith(provider: ConnectionProvider): DSLContext {
        return DefaultConfiguration()
            .set(provider)
            .set(dialect)
            .set(createSettings())
            .dsl()
    }

    private fun createSettings(): Settings {
        return Settings()
            .withRenderSchema(false)
            .withRenderMapping(
                RenderMapping()
                    .withSchemata(
                        MappedSchema()
                            .withInputExpression(MATCH_ALL_EXCEPT_INFORMATION_SCHEMA.toPattern())
                            .withTables(
                                MappedTable()
                                    .withInputExpression(MATCH_ALL.toPattern())
                                    .withOutput(REPLACEMENT)
                            )
                    )
            )
    }
}

val Vendor.dialect: SQLDialect
    get() = when (this) {
        Vendor.HSQLDB -> SQLDialect.HSQLDB
        Vendor.MYSQL -> SQLDialect.MYSQL
        Vendor.MARIADB -> SQLDialect.MARIADB
        Vendor.POSTGRESQL -> SQLDialect.POSTGRES
    }

val SQLDatabase.context: JooqContext get() = JooqContext(vendor.dialect)

data class Transaction(
    val context: JooqContext,
    val connection: Connection
) : DSLContext by context.createContext(connection)

inline fun <R> transaction(
    coroutineScope: CoroutineScope = ChuuniCorePlugin.getPlugin(),
    database: SQLDatabase = ChuuniCorePlugin.getPlugin().scope.getInstance<SQLDatabase>(),
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend Transaction.() -> R
): Deferred<R> {
    return coroutineScope.async(context) {
        blockingTransaction(database) {
            block()
        }
    }
}

@Blocking
inline fun <R> blockingTransaction(
    database: SQLDatabase = ChuuniCorePlugin.getPlugin().scope.getInstance<SQLDatabase>(),
    block: Transaction.() -> R
): R {
    val jooqContext = database.context
    runCatching {
        block(Transaction(jooqContext, database.connection))
    }.fold({
        database.connection.commit()
        return it
    }) {
        database.connection.rollback()
        throw it
    }
}