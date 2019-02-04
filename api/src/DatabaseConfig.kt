package me.guillaumewilmot.api

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.guillaumewilmot.api.models.Exercises
import me.guillaumewilmot.api.models.Lifts
import me.guillaumewilmot.api.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConfig
import java.io.File
import java.sql.Connection

object DB {
    private lateinit var db: Database
    private const val isolation = Connection.TRANSACTION_SERIALIZABLE
    private val repetitions = TransactionManager.manager.defaultRepetitionAttempts

    suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction(db) {
            block()
        }
    }

    fun connect() {
        db = Database.connect(hikari())
        val manager = ThreadLocalTransactionManager(db, isolation, repetitions)
        TransactionManager.registerManager(db, manager)
        TransactionManager.resetCurrent(manager)
        migrate()
    }

    private fun migrate() {
        transaction(db) {
            SchemaUtils.create(Lifts)
            SchemaUtils.create(Exercises)
            SchemaUtils.create(Users)
        }
    }

    private fun hikari(): HikariDataSource = HikariDataSource(
        HikariConfig()
            .apply { driverClassName = "org.sqlite.JDBC" }
            .apply { jdbcUrl = "jdbc:sqlite:" + sqlitePath() }
            .apply { maximumPoolSize = 5 }
            .apply { validate() }
            .apply { dataSourceProperties = SQLiteConfig().apply { enforceForeignKeys(true) }.toProperties() }
    )

    @Throws
    private fun sqlitePath() = File("database.sqlite").apply {
        if (!exists()) {
            createNewFile()
        }
    }
}