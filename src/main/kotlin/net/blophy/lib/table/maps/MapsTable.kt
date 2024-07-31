package net.blophy.lib.table.maps

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

enum class MapStatus(i: Int) { Ranked(3), Approved(2), Pending(1), WIP(0), Graveyard(-1) }

@Serializable
data class ScoreUploadInfo(
    val userName: String,
    val score: Int,
    val startTimeStamp: Int,
    val endTimeStamp: Int,
    val submitTimeStamp: Int,
    val mapID: Int
)

object Maps : Table("MapsData") {
    val id = integer("id").autoIncrement()
    val name = text("name").uniqueIndex()
    val description = text("description")
    val author = text("author")
    val status = integer("status")
    val stars = double("stars")
    val length = integer("length") // 单位是秒
    override val primaryKey = PrimaryKey(id)
}

data class MapsTable(
    val id: Int, val name: String, val description: String,
    val author: String, val status: Int, val stars: Double, val length: Int
)

fun ResultRow.toMapsTable() = MapsTable(
    id = this[Maps.id],
    name = this[Maps.name],
    description = this[Maps.description],
    author = this[Maps.author],
    status = this[Maps.status],
    stars = this[Maps.stars],
    length = this[Maps.length],
)
