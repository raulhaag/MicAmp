package ar.rulosoft.micamp.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import ar.rulosoft.micamp.ui.SvgIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    saveDir: Uri?,
    onDirectorySelected: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<DocumentFile>()) }
    var playingFileUri by remember { mutableStateOf<Uri?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    // Directory Picker
    val dirPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            onDirectorySelected(uri)
        }
    }

    LaunchedEffect(saveDir) {
        if (saveDir != null) {
            val root = DocumentFile.fromTreeUri(context, saveDir)
            files = root?.listFiles()
                ?.filter { it.name?.endsWith(".wav", ignoreCase = true) == true }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else {
            files = emptyList()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    fun stopPlayer() {
        try {
            player?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player?.release()
        player = null
        playingFileUri = null
    }

    fun play(file: DocumentFile) {
        if (playingFileUri == file.uri) {
            stopPlayer()
        } else {
            stopPlayer()
            try {
                player = MediaPlayer().apply {
                    setDataSource(context, file.uri)
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopPlayer()
                    }
                }
                playingFileUri = file.uri
            } catch (e: Exception) {
                e.printStackTrace()
                stopPlayer()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grabaciones")
                        val dirName = if (saveDir != null) {
                            DocumentFile.fromTreeUri(context, saveDir)?.name ?: "Desconocido"
                        } else "Sin carpeta"
                        Text(dirName, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { dirPicker.launch(null) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Cambiar Carpeta")
                    }
                }
            )
        }
    ) { padding ->
        if (saveDir == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se ha seleccionado carpeta de grabaciones")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { dirPicker.launch(null) }) {
                        Text("Seleccionar Carpeta")
                    }
                }
            }
        } else if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay archivos .wav en esta carpeta")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->
                    val isPlaying = playingFileUri == file.uri
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        onClick = { play(file) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = file.name ?: "Desconocido", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "${file.length() / 1024} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Icon(
                                imageVector = if (isPlaying) SvgIcons.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Parar" else "Reproducir",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
