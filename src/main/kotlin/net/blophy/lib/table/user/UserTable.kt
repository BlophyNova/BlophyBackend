package net.blophy.lib.table.user

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Users : Table("UserData") {
    val id = integer("id").autoIncrement()
    val username = text("username").uniqueIndex()
    val password = text("password")
    val totalScore = integer("totalScore")
    val playedMaps = integer("playedMaps")
    val stageRank = float("stageRank")
    val juiceRank = float("juiceRank")
    val lemon = integer("lemon") // 单位mL
    val banned = bool("banned").default(false)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class User(
    val id: Int, val username: String, val password: String, val totalScore: Int,
    val playedMaps: Int, val stageRank: Float, val juiceRank: Float, val lemon: Int, val banned: Boolean,
)

fun ResultRow.toUser() = User(
    id = this[Users.id],
    username = this[Users.username],
    password = this[Users.password],
    totalScore = this[Users.totalScore],
    playedMaps = this[Users.playedMaps],
    stageRank = this[Users.stageRank],
    juiceRank = this[Users.juiceRank],
    lemon = this[Users.lemon],
    banned = this[Users.banned],
)

@Serializable
data class Credentials(val username: String, val password: String)

@Serializable
data class UserSession(val username: String)
