package id.mport.maps.ui.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.data.entity.SurveyEntity
import id.mport.maps.domain.measurement.PointCodec
import id.mport.maps.domain.unit.UnitFormatter
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.export.SurveyExporter
import id.mport.maps.viewmodel.SurveyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    padding: PaddingValues,
    vm: SurveyViewModel,
    onOpenSurvey: () -> Unit = {}
) {
    val surveys by vm.surveys.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var exportTarget by remember { mutableStateOf<SurveyEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text("Riwayat Survey", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (surveys.isEmpty()) {
            Text("Belum ada survey tersimpan.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(surveys, key = { it.id }) { survey ->
                    SurveyCard(
                        survey = survey,
                        onDelete = { vm.deleteSurvey(survey) },
                        onRestore = {
                            vm.restoreSurvey(survey)
                            onOpenSurvey()
                        },
                        onExport = { exportTarget = survey }
                    )
                }
            }
        }
    }

    exportTarget?.let { survey ->
        ExportDialog(
            survey = survey,
            onDismiss = { exportTarget = null },
            onExport = { format ->
                val ok = when (format) {
                    "csv" -> SurveyExporter.saveToDownloads(
                        context,
                        SurveyExporter.defaultFileName(survey, "csv"),
                        "text/csv",
                        SurveyExporter.toCsv(survey)
                    )
                    "json" -> SurveyExporter.saveToDownloads(
                        context,
                        SurveyExporter.defaultFileName(survey, "json"),
                        "application/json",
                        SurveyExporter.toJson(survey)
                    )
                    "kml" -> SurveyExporter.saveToDownloads(
                        context,
                        SurveyExporter.defaultFileName(survey, "kml"),
                        "application/vnd.google-earth.kml+xml",
                        SurveyExporter.toKml(survey)
                    )
                    else -> false
                }
                val msg = when {
                    !ok -> "Gagal export"
                    android.os.Build.VERSION.SDK_INT >= 29 -> "Tersimpan di Downloads/MPorT Maps"
                    else -> "Tersimpan di folder aplikasi (Downloads/MPorT Maps)"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                exportTarget = null
            }
        )
    }
}

@Composable
private fun SurveyCard(
    survey: SurveyEntity,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit
) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(survey.createdAt))
    val unit = UnitMode.fromStored(survey.unit)
    val points = PointCodec.decode(survey.pointsJson)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(survey.name, style = MaterialTheme.typography.titleMedium)
            if (survey.mode == "AREA") {
                Text("Luas: ${UnitFormatter.area(survey.areaSquareMeters, unit)}")
                Text("Keliling: ${UnitFormatter.distance(survey.perimeterMeters, unit)}")
            } else {
                Text("Jarak: ${UnitFormatter.distance(survey.distanceMeters, unit)}")
            }
            Text("${points.size} titik • $date")
            if (survey.notes.isNotBlank()) Text(survey.notes)
            Row {
                TextButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Buka")
                }
                TextButton(onClick = onExport) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus")
                }
            }
        }
    }
}

@Composable
private fun ExportDialog(
    survey: SurveyEntity,
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export: ${survey.name}") },
        text = { Text("Pilih format file. File disimpan ke Downloads/MPorT Maps.") },
        confirmButton = {
            Row {
                TextButton(onClick = { onExport("csv") }) { Text("CSV") }
                TextButton(onClick = { onExport("json") }) { Text("JSON") }
                TextButton(onClick = { onExport("kml") }) { Text("KML") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
