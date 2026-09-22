package id.mport.maps.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.mport.maps.data.entity.MarkerEntity
import id.mport.maps.data.entity.NoteEntity
import id.mport.maps.data.entity.SurveyEntity

@Database(
    entities = [SurveyEntity::class, MarkerEntity::class, NoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
    abstract fun markerDao(): MarkerDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: SurveyDatabase? = null

        fun getInstance(context: Context): SurveyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SurveyDatabase::class.java,
                    "mport_maps.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
