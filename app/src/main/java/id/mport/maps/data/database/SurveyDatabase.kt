package id.mport.maps.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        /**
         * v1 → v2: schema already matches current entities (surveys/markers/notes).
         * Explicit no-op migration preserves user data instead of wiping via destructive path.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No structural change required for this version step.
            }
        }

        fun getInstance(context: Context): SurveyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SurveyDatabase::class.java,
                    "mport_maps.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Safety net only for unexpected/unknown version jumps
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
