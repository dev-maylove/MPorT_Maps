package id.mport.maps.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surveys")
data class SurveyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: String,                 // DISTANCE | AREA
    val pointsJson: String,           // encoded points
    val distanceMeters: Double,
    val areaSquareMeters: Double,
    val perimeterMeters: Double,
    val notes: String = "",
    val unit: String = "METER",
    val mapType: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
