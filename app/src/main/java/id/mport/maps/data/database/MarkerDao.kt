package id.mport.maps.data.database

import androidx.room.*
import id.mport.maps.data.entity.MarkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {
    @Insert suspend fun insert(item: MarkerEntity): Long
    @Delete suspend fun delete(item: MarkerEntity)
    @Query("SELECT * FROM markers ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MarkerEntity>>
    @Query("DELETE FROM markers")
    suspend fun deleteAll()
}
