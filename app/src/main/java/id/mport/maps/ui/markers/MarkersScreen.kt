package id.mport.maps.ui.markers

import id.mport.maps.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.data.entity.MarkerEntity
import id.mport.maps.viewmodel.SurveyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MarkersScreen(padding: PaddingValues, vm: SurveyViewModel) {
    val markers by vm.markers.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.markers_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.markers_hint))
        Spacer(Modifier.height(12.dp))
        if (markers.isEmpty()) {
            Text(stringResource(R.string.markers_empty))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(markers, key = { it.id }) { marker ->
                    MarkerCard(marker) { vm.deleteMarker(marker) }
                }
            }
        }
    }
}

@Composable
private fun MarkerCard(marker: MarkerEntity, onDelete: () -> Unit) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(marker.createdAt))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(marker.title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            Text("%.6f, %.6f".format(marker.latitude, marker.longitude))
            if (marker.notes.isNotBlank()) Text(marker.notes)
            Text(date)
        }
    }
}
