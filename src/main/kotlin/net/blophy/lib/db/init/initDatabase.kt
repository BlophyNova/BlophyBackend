package net.blophy.lib.db.init

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.blophy.lib.table.user.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase() {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://gz-postgres-0uy7hm97.sql.tencentcdb.com:21859/UserData"
        username = "root"
        password = "blophyNova0!"
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    })

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users)
    }
}