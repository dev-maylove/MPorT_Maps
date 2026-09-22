package id.mport.maps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.mport.maps.data.AppSettings
import id.mport.maps.data.SettingsStore
import id.mport.maps.data.database.SurveyDatabase
import id.mport.maps.data.entity.MarkerEntity
import id.mport.maps.data.entity.NoteEntity
import id.mport.maps.data.entity.SurveyEntity
import id.mport.maps.data.repository.SurveyRepository
import id.mport.maps.domain.measurement.*
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.location.LocationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SurveyUiState(
    val mode: MeasurementMode = MeasurementMode.DISTANCE,
    val points: List<MeasurementPoint> = emptyList(),
    val distanceMeters: Double = 0.0,
    val areaSquareMeters: Double = 0.0,
    val perimeterMeters: Double = 0.0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val gpsLoading: Boolean = false,
    val message: String? = null,
    val savedOk: Boolean = false
)

class SurveyViewModel(app: Application) : AndroidViewModel(app) {
    private val db = SurveyDatabase.getInstance(app)
    private val repo = SurveyRepository(db)
    private val location = LocationManager(app)
    private val settingsStore = SettingsStore(app)

    val surveys = repo.surveys.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val markers = repo.markers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notes = repo.notes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _ui = MutableStateFlow(SurveyUiState())
    val ui: StateFlow<SurveyUiState> = _ui.asStateFlow()

    private val undoStack = ArrayDeque<List<MeasurementPoint>>()
    private val redoStack = ArrayDeque<List<MeasurementPoint>>()

    fun setMode(mode: MeasurementMode) {
        _ui.update { it.copy(mode = mode) }
        recalculate()
    }

    fun addPoint(point: MeasurementPoint) {
        pushUndo()
        redoStack.clear()
        _ui.update { it.copy(points = it.points + point) }
        recalculate()
    }

    fun addLatLng(lat: Double, lng: Double) {
        addPoint(MeasurementPoint(latitude = lat, longitude = lng))
    }

    fun undo() {
        val current = _ui.value.points
        if (current.isEmpty()) return
        redoStack.addLast(current)
        val previous = undoStack.removeLastOrNull() ?: emptyList()
        _ui.update { it.copy(points = previous) }
        recalculate()
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        pushUndo()
        _ui.update { it.copy(points = next) }
        recalculate()
    }

    fun clearPoints() {
        if (_ui.value.points.isEmpty()) return
        pushUndo()
        redoStack.clear()
        _ui.update { it.copy(points = emptyList()) }
        recalculate()
    }

    fun locate() {
        viewModelScope.launch {
            _ui.update { it.copy(gpsLoading = true, message = null) }
            val fix = runCatching { location.current() }.getOrNull()
            if (fix == null) {
                _ui.update {
                    it.copy(
                        gpsLoading = false,
                        message = "GPS tidak mendapatkan lokasi. Pastikan GPS aktif."
                    )
                }
            } else {
                addPoint(fix)
                _ui.update { it.copy(gpsLoading = false, message = null) }
            }
        }
    }

    fun saveSurvey(name: String, notes: String) {
        val s = _ui.value
        if (s.points.isEmpty()) return
        viewModelScope.launch {
            val unit = settings.value.unit
            val mapType = settings.value.mapType
            repo.saveSurvey(
                SurveyEntity(
                    name = name.ifBlank {
                        if (s.mode == MeasurementMode.AREA) "Survey Area" else "Survey Jarak"
                    },
                    mode = s.mode.name,
                    pointsJson = PointCodec.encode(s.points),
                    distanceMeters = s.distanceMeters,
                    areaSquareMeters = s.areaSquareMeters,
                    perimeterMeters = s.perimeterMeters,
                    notes = notes,
                    unit = unit.name,
                    mapType = mapType
                )
            )
            // Reset measurement state fully after successful save
            undoStack.clear()
            redoStack.clear()
            _ui.update {
                it.copy(
                    points = emptyList(),
                    distanceMeters = 0.0,
                    areaSquareMeters = 0.0,
                    perimeterMeters = 0.0,
                    canUndo = false,
                    canRedo = false,
                    savedOk = true,
                    message = "Survey berhasil disimpan"
                )
            }
        }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null, savedOk = false) }
    }

    fun deleteSurvey(survey: SurveyEntity) {
        viewModelScope.launch { repo.deleteSurvey(survey) }
    }

    fun saveMarker(title: String, lat: Double, lng: Double, notes: String = "") {
        viewModelScope.launch {
            repo.saveMarker(
                MarkerEntity(title = title.ifBlank { "Marker" }, latitude = lat, longitude = lng, notes = notes)
            )
        }
    }

    fun deleteMarker(marker: MarkerEntity) {
        viewModelScope.launch { repo.deleteMarker(marker) }
    }

    fun saveNote(title: String, body: String, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            repo.saveNote(NoteEntity(title = title, body = body, latitude = lat, longitude = lng))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repo.deleteNote(note) }
    }

    fun setMapType(v: Int) {
        viewModelScope.launch { settingsStore.setMapType(v) }
    }

    fun setUnit(v: UnitMode) {
        viewModelScope.launch { settingsStore.setUnit(v) }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch { settingsStore.setDarkTheme(dark) }
    }

    fun restoreSurvey(survey: SurveyEntity) {
        val points = PointCodec.decode(survey.pointsJson)
        val mode = runCatching { MeasurementMode.valueOf(survey.mode) }
            .getOrDefault(MeasurementMode.DISTANCE)
        undoStack.clear()
        redoStack.clear()
        _ui.update {
            it.copy(
                mode = mode,
                points = points,
                distanceMeters = survey.distanceMeters,
                areaSquareMeters = survey.areaSquareMeters,
                perimeterMeters = survey.perimeterMeters,
                canUndo = false,
                canRedo = false
            )
        }
    }

    private fun pushUndo() {
        undoStack.addLast(_ui.value.points)
        if (undoStack.size > 50) undoStack.removeFirst()
    }

    private fun recalculate() {
        val points = _ui.value.points
        val dist = DistanceCalculator.pathLength(points)
        val area = if (_ui.value.mode == MeasurementMode.AREA) AreaCalculator.polygonArea(points) else 0.0
        val peri = if (_ui.value.mode == MeasurementMode.AREA) AreaCalculator.perimeter(points) else dist
        _ui.update {
            it.copy(
                distanceMeters = dist,
                areaSquareMeters = area,
                perimeterMeters = peri,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }
}
