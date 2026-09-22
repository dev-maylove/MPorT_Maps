package id.mport.maps.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
