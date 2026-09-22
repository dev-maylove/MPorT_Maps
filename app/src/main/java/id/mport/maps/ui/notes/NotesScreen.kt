package id.mport.maps.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.data.entity.NoteEntity
import id.mport.maps.viewmodel.SurveyViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(padding: PaddingValues, vm: SurveyViewModel) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Catatan", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
        Spacer(Modifier.height(12.dp))
        if (notes.isEmpty()) {
            Text("Belum ada catatan.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note) { vm.deleteNote(note) }
                }
            }
        }
    }

    if (showAdd) {
        AddNoteDialog(
            onDismiss = { showAdd = false },
            onSave = { title, body ->
                vm.saveNote(title, body)
                showAdd = false
            }
        )
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onDelete: () -> Unit) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(note.createdAt))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                }
            }
            Text(note.body)
            Text(date)
        }
    }
}

@Composable
private fun AddNoteDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catatan Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, singleLine = true)
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Isi") }, minLines = 4)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.ifBlank { "Catatan" }, body) },
                enabled = body.isNotBlank()
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
