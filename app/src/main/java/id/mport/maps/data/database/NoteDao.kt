package id.mport.maps.data.database

import androidx.room.*
import id.mport.maps.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert suspend fun insert(item: NoteEntity): Long
    @Delete suspend fun delete(item: NoteEntity)
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
