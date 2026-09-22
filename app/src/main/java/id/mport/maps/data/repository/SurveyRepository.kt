package id.mport.maps.data.repository

import id.mport.maps.data.database.SurveyDatabase
import id.mport.maps.data.entity.MarkerEntity
import id.mport.maps.data.entity.NoteEntity
import id.mport.maps.data.entity.SurveyEntity
import kotlinx.coroutines.flow.Flow

class SurveyRepository(private val db: SurveyDatabase) {
    val surveys: Flow<List<SurveyEntity>> = db.surveyDao().observeAll()
    val markers: Flow<List<MarkerEntity>> = db.markerDao().observeAll()
    val notes: Flow<List<NoteEntity>> = db.noteDao().observeAll()

    suspend fun saveSurvey(survey: SurveyEntity) = db.surveyDao().insert(survey)
    suspend fun deleteSurvey(survey: SurveyEntity) = db.surveyDao().delete(survey)
    suspend fun getSurvey(id: Long) = db.surveyDao().getById(id)

    suspend fun saveMarker(marker: MarkerEntity) = db.markerDao().insert(marker)
    suspend fun deleteMarker(marker: MarkerEntity) = db.markerDao().delete(marker)

    suspend fun saveNote(note: NoteEntity) = db.noteDao().insert(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().delete(note)
}
