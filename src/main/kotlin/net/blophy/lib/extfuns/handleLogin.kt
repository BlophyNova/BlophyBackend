package net.blophy.lib.extfuns

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import net.blophy.lib.config.JwtConfig.generateToken
import net.blophy.lib.table.user.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun findUserByUsername(username: String): User? {
    return Users.selectAll().where { Users.username eq username }
        .map { it.toUser() }
        .singleOrNull()
}

suspend fun ApplicationCall.handleLogin() {
    val (username, password) = receive<Credentials>()
    val user = transaction { findUserByUsername(username) }

    if (user != null) {
        if (user.password == password) {
            val token = generateToken(user.username)
            respond(mapOf("token" to token))
            // 将用户信息存储在会话中
            // 估计以后要换redis,先用着吧
            sessions.set(UserSession(user.username))
        } else {
            respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }
}
