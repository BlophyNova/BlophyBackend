package net.blophy.lib.db.man

import net.blophy.lib.table.user.User
import net.blophy.lib.table.user.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun insertOrUpdateUser(user: User) {
    transaction {
        // 检查用户是否已存在
        val existingUser = Users.selectAll().where { Users.id eq user.id }.singleOrNull()

        if (existingUser != null) {
            // 如果用户存在，更新记录
            Users.update({ Users.id eq user.id }) {
                it[username] = user.username
                it[password] = user.password
            }
        } else {
            // 如果用户不存在，插入新记录
            Users.insert {
                it[username] = user.username
                it[password] = user.password
            }
        }
    }
}