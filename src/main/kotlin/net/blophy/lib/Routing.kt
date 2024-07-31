package net.blophy.lib

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import net.blophy.lib.extfuns.findUserByUsername
import net.blophy.lib.extfuns.handleLogin
import net.blophy.lib.table.user.User
import net.blophy.lib.table.user.UserSession
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun Application.configureRouting() {
    routing {
        authenticate {
            post("/login") { call.handleLogin() }
            get("/user/{username}/profile") {
                val username = call.parameters["username"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val session = call.sessions.get<UserSession>()
                if (session == null || session.username != username) {
                    call.respond(HttpStatusCode.Unauthorized)
                }
                val info = findUserByUsername(username)
                if (info == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                call.respond(Json.encodeToString(User.serializer(), info!!))
            }
            webSocket("/user/{username}/upload/avatar") {
                val username = call.parameters["username"] ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "Username missing"
                    )
                )
                val session = call.sessions.get<UserSession>()
                if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                if (session == null || session.username != username) {
                    call.respond(HttpStatusCode.Unauthorized)
                }
                // 创建用户目录
                val userDir = File("backend/data/users/$username")
                if (!userDir.exists()) {
                    userDir.mkdirs() // 创建目录
                }

                val avatarFile = File(userDir, "avatar.png")
                val outputStream = FileOutputStream(avatarFile)

                val pngSignature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) // 这一坨是png文件的签名
                val buffer = ByteArray(8) // 用于存储签名的缓冲区

                val maxFileSize = 5 * 1024 * 1024 // 最大5MB的头像
                var totalBytesWritten = 0

                try {
                    val firstFrame = incoming.receive()
                    if (firstFrame is Frame.Binary) {
                        // 检查数据长度是否足够
                        if (firstFrame.data.size < 8) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@webSocket
                        }
                        System.arraycopy(firstFrame.data, 0, buffer, 0, 8)
                        if (!buffer.contentEquals(pngSignature)) {
                            call.respond(HttpStatusCode.BadRequest)
                            return@webSocket
                        }
                        outputStream.write(firstFrame.data)
                        while (true) {
                            val frame = incoming.receive()
                            if (frame is Frame.Binary) {
                                if (totalBytesWritten + frame.data.size > maxFileSize) {
                                    call.respond(HttpStatusCode.BadRequest, "File size exceeds limit.")
                                    break
                                }
                                outputStream.write(frame.data)
                                totalBytesWritten += frame.data.size
                            }
                        }
                    }
                } catch (e: ClosedReceiveChannelException) {
                    // WebSocket关闭时处理
                } catch (e: IOException) {
                    // 处理文件写入错误
                    call.respond(HttpStatusCode.InternalServerError, "File upload failed: ${e.message}")
                } finally {
                    outputStream.close()
                    close(CloseReason(CloseReason.Codes.NORMAL, "File upload complete"))
                    call.respond(HttpStatusCode.OK, "Avatar uploaded successfully.")
                }
            }
        }
    }
}
