package net.blophy

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import net.blophy.lib.config.JwtConfig
import net.blophy.lib.configureRouting
import net.blophy.lib.extfuns.findUserByUsername
import net.blophy.lib.table.user.UserSession
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
    }
    install(Sessions) {
        cookie<UserSession>("SESSION") {
            cookie.path = "/" // Cookie 的路径
            cookie.httpOnly = true // 阻止从js访问
            cookie.secure = false // 正式部署之前设为true
        }
    }
    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val username = credential.payload.subject
                if (username != null) {
                    val user = transaction { findUserByUsername(username) }
                    if (user != null && !user.banned) {
                        UserIdPrincipal(user.username) // 返回用户的身份
                    } else {
                        null // 用户不存在或被ban了
                    }
                } else {
                    null // 无效的 JWT
                }
            }
        }
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    configureRouting()
}
