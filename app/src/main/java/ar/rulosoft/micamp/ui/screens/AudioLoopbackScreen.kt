package ar.rulosoft.micamp.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.rulosoft.micamp.ui.SvgIcons
import ar.rulosoft.micamp.ui.controls.DeviceSelector
import ar.rulosoft.micamp.ui.controls.SaveRecordingDialog
import ar.rulosoft.micamp.ui.viewmodels.AudioViewModel
import ar.rulosoft.micamp.ui.viewmodels.AudioViewModelFactory
import java.io.File

@Composable
fun AudioLoopbackScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: AudioViewModel = viewModel(factory = AudioViewModelFactory(context))
    
    var showRecordingsScreen by remember { mutableStateOf(false) }
    var currentDspEffect by remember { mutableStateOf(DspEffect.NONE) }

    val snackbarHostState = remember { SnackbarHostState() }
    
    if (viewModel.showSaveDialog) {
        SaveRecordingDialog(
            initialPath = viewModel.lastRecordedPath,
            onSave = { newName ->
                val file = File(viewModel.lastRecordedPath)
                if (viewModel.currentSaveDir != null) {
                     try {
                         val docFile = DocumentFile.fromTreeUri(context, viewModel.currentSaveDir!!)
                         val newDoc = docFile?.createFile("audio/wav", "$newName.wav")
                         if (newDoc != null) {
                             context.contentResolver.openOutputStream(newDoc.uri)?.use { out ->
                                 file.inputStream().use { it.copyTo(out) }
                             }
                             file.delete() 
                         }
                     } catch(e: Exception) {
                         e.printStackTrace()
                     }
                } else {
                    val newFile = File(file.parent, "$newName.wav")
                    file.renameTo(newFile)
                }
                viewModel.showSaveDialog = false
            },
            onDiscard = {
                val file = File(viewModel.lastRecordedPath)
                if (file.exists()) file.delete()
                viewModel.showSaveDialog = false
            },
            onDismiss = {
                viewModel.showSaveDialog = false
            }
        )
    }

    if (currentDspEffect != DspEffect.NONE) {
        BackHandler { currentDspEffect = DspEffect.NONE }
        DspSettingsScreen(
            effect = currentDspEffect,
            onBack = { currentDspEffect = DspEffect.NONE },
            
            distortion = viewModel.distortion,
            onDistortionChange = { viewModel.distortion = it },
            
            eqBands = viewModel.eqBands,
            bandLabels = listOf("60", "230", "910", "3k", "14k", "20k"),
            
            delayTime = viewModel.delayTime,
            onDelayTimeChange = { viewModel.delayTime = it },
            delayFeedback = viewModel.delayFeedback,
            onDelayFeedbackChange = { viewModel.delayFeedback = it },
            delayMix = viewModel.delayMix,
            onDelayMixChange = { viewModel.delayMix = it },
            
            reverbMix = viewModel.reverbMix,
            onReverbMixChange = { viewModel.reverbMix = it },
            reverbSize = viewModel.reverbSize,
            onReverbSizeChange = { viewModel.reverbSize = it },
            
            compThreshold = viewModel.compThreshold,
            onCompThresholdChange = { viewModel.compThreshold = it },
            compRatio = viewModel.compRatio,
            onCompRatioChange = { viewModel.compRatio = it },
            compMakeup = viewModel.compMakeup,
            onCompMakeupChange = { viewModel.compMakeup = it },
            
            tremDepth = viewModel.tremDepth,
            onTremDepthChange = { viewModel.tremDepth = it },
            tremRate = viewModel.tremRate,
            onTremRateChange = { viewModel.tremRate = it },
            
            chorusRate = viewModel.chorusRate,
            onChorusRateChange = { viewModel.chorusRate = it },
            chorusDepth = viewModel.chorusDepth,
            onChorusDepthChange = { viewModel.chorusDepth = it },
            chorusMix = viewModel.chorusMix,
            onChorusMixChange = { viewModel.chorusMix = it }
        )
    } else if (showRecordingsScreen) {
        RecordingsScreen(
            saveDir = viewModel.currentSaveDir,
            onDirectorySelected = { uri -> 
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                viewModel.setSaveDirectory(uri)
            },
            onClose = { showRecordingsScreen = false }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                 if (viewModel.isRunning) {
                     BottomAppBar {
                         Row(
                             modifier = Modifier.fillMaxWidth().padding(16.dp),
                             verticalAlignment = Alignment.CenterVertically,
                             horizontalArrangement = Arrangement.SpaceBetween
                         ) {
                             Text(
                                 text = if (viewModel.isRecording) "REC: ${formatTime(viewModel.recordingSeconds)}" else "En vivo",
                                 color = if (viewModel.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                             )
                             
                             IconButton(onClick = { viewModel.toggleRecording() }) {
                                 Icon(
                                     imageVector = if (viewModel.isRecording) SvgIcons.Stop else SvgIcons.Record,
                                     contentDescription = "Grabar",
                                     tint = if (viewModel.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                 )
                             }
                         }
                     }
                 }
            }
        ) { padding ->
            Column(modifier = modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text(if (viewModel.isRunning) "Monitor en Vivo" else "Estudio de Audio", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!viewModel.isRunning) {
                    // CONFIGURATION MODE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Text("Carpeta de grabaciones:", style = MaterialTheme.typography.bodySmall)
                         Spacer(modifier = Modifier.width(8.dp))
                         val dirName = if (viewModel.currentSaveDir != null) {
                            DocumentFile.fromTreeUri(context, viewModel.currentSaveDir!!)?.name ?: "Desconocido"
                        } else "Por defecto (App)"
                        Text(dirName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    DeviceSelector(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                        inputDevices = viewModel.inputDevices,
                        outputDevices = viewModel.outputDevices,
                        selectedInputDevice = viewModel.selectedInputDevice,
                        selectedOutputDevice = viewModel.selectedOutputDevice,
                        onInputSelected = { viewModel.selectedInputDevice = it },
                        onOutputSelected = { viewModel.selectedOutputDevice = it }
                    )

                } else {
                    // PERFORMANCE MODE
                    PerformanceScreen(
                        modifier = Modifier.weight(1f),
                        visualizerData = viewModel.visualizerData,
                        
                        isDistortionEnabled = viewModel.isDistortionEnabled,
                        onDistortionEnabledChange = { viewModel.isDistortionEnabled = it },
                        
                        isEqEnabled = viewModel.isEqEnabled,
                        onEqEnabledChange = { viewModel.isEqEnabled = it },
                        
                        isDelayEnabled = viewModel.isDelayEnabled,
                        onDelayEnabledChange = { viewModel.isDelayEnabled = it },
                        
                        isReverbEnabled = viewModel.isReverbEnabled,
                        onReverbEnabledChange = { viewModel.isReverbEnabled = it },
                        
                        isCompressorEnabled = viewModel.isCompressorEnabled,
                        onCompressorEnabledChange = { viewModel.isCompressorEnabled = it },
                        
                        isTremoloEnabled = viewModel.isTremoloEnabled,
                        onTremoloEnabledChange = { viewModel.isTremoloEnabled = it },
                        
                        isChorusEnabled = viewModel.isChorusEnabled,
                        onChorusEnabledChange = { viewModel.isChorusEnabled = it },
                        
                        onOpenSettings = { currentDspEffect = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Ganancia Master: ${(viewModel.volume * 100).toInt()}%")
                Slider(value = viewModel.volume, onValueChange = { viewModel.volume = it }, valueRange = 0f..5f, steps = 9)

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!viewModel.isRunning) {
                        Button(
                            onClick = { showRecordingsScreen = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Grabaciones")
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.toggleEngine() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (viewModel.isRunning) "APAGAR" else "ENCENDER")
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
