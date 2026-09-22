package id.mport.maps.data.database

import androidx.room.*
import id.mport.maps.data.entity.SurveyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Insert suspend fun insert(item: SurveyEntity): Long
    @Delete suspend fun delete(item: SurveyEntity)
    @Query("SELECT * FROM surveys ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SurveyEntity>>
    @Query("SELECT * FROM surveys WHERE id = :id")
    suspend fun getById(id: Long): SurveyEntity?
    @Query("DELETE FROM surveys")
    suspend fun deleteAll()
}
