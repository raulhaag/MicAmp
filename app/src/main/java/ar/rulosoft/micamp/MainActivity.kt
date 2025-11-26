package ar.rulosoft.micamp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import ar.rulosoft.micamp.ui.theme.MicAmpTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MicAmpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MicAmpApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MicAmpApp(modifier: Modifier = Modifier) {
    var hasPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            hasPermission = true
        }
    }

    if (hasPermission) {
        AudioLoopbackScreen(modifier)
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de grabación de audio para funcionar.")
        }
    }
}

@Composable
fun AudioLoopbackScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val prefs = remember { context.getSharedPreferences("MicAmpPrefs", Context.MODE_PRIVATE) }
    
    var inputDevices by remember { mutableStateOf(listOf<AudioDeviceInfo>()) }
    var outputDevices by remember { mutableStateOf(listOf<AudioDeviceInfo>()) }
    
    var selectedInputDevice by remember { mutableStateOf<AudioDeviceInfo?>(null) }
    var selectedOutputDevice by remember { mutableStateOf<AudioDeviceInfo?>(null) }
    
    var isRunning by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var volume by remember { mutableFloatStateOf(1.0f) }
    
    // Dialog State
    var showSaveDialog by remember { mutableStateOf(false) }
    var lastRecordedPath by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Coordinates for drawing the cable
    var inputPoint by remember { mutableStateOf(Offset.Unspecified) }
    var outputPoint by remember { mutableStateOf(Offset.Unspecified) }
    var canvasPosition by remember { mutableStateOf(Offset.Zero) }

    // Function to update device lists
    fun updateDevices() {
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
        inputDevices = inputs
        
        val savedInputName = prefs.getString("last_input_name", null)
        
        if (selectedInputDevice == null) {
            if (savedInputName != null) {
                selectedInputDevice = inputs.find { it.productName == savedInputName }
            }
            if (selectedInputDevice == null || !inputs.any { it.id == selectedInputDevice?.id }) {
                selectedInputDevice = inputs.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC } ?: inputs.firstOrNull()
            }
        } else {
             selectedInputDevice = inputs.find { it.id == selectedInputDevice?.id }
        }

        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
        outputDevices = outputs
        
        val savedOutputName = prefs.getString("last_output_name", null)

        if (selectedOutputDevice == null) {
             if (savedOutputName != null) {
                selectedOutputDevice = outputs.find { it.productName == savedOutputName }
            }
            if (selectedOutputDevice == null || !outputs.any { it.id == selectedOutputDevice?.id }) {
                selectedOutputDevice = outputs.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER } ?: outputs.firstOrNull()
            }
        } else {
             selectedOutputDevice = outputs.find { it.id == selectedOutputDevice?.id }
        }
    }
    
    LaunchedEffect(selectedInputDevice) {
        selectedInputDevice?.let {
            prefs.edit().putString("last_input_name", it.productName.toString()).apply()
        }
    }
    
    LaunchedEffect(selectedOutputDevice) {
        selectedOutputDevice?.let {
            prefs.edit().putString("last_output_name", it.productName.toString()).apply()
        }
    }

    DisposableEffect(audioManager) {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
        }
        audioManager.registerAudioDeviceCallback(callback, null)
        updateDevices()
        onDispose {
            audioManager.unregisterAudioDeviceCallback(callback)
        }
    }

    val engineRef = remember { mutableStateOf<AudioEngine?>(null) }
    
    LaunchedEffect(isRecording) {
        if (isRecording) recordingSeconds = 0
    }

    LaunchedEffect(isRunning, selectedInputDevice, selectedOutputDevice) {
        engineRef.value?.stop()
        engineRef.value = null
        
        if (isRunning && selectedInputDevice != null && selectedOutputDevice != null) {
            val engine = AudioEngine(
                context = context,
                inputDevice = selectedInputDevice!!,
                outputDevice = selectedOutputDevice!!,
                getVolume = { volume },
                onRecordingError = {
                     isRecording = false
                },
                onRecordingSaved = { },
                onRecordingProgress = { seconds ->
                    recordingSeconds = seconds
                }
            )
            engine.onRecordingSavedUI = { path ->
                lastRecordedPath = path
                showSaveDialog = true
            }
            
            engine.start()
            engine.setRecording(isRecording)
            engineRef.value = engine
        }
    }
    
    LaunchedEffect(isRecording) {
        engineRef.value?.setRecording(isRecording)
    }

    DisposableEffect(Unit) {
        onDispose { engineRef.value?.stop() }
    }
    
    // Save Dialog
    if (showSaveDialog) {
        SaveRecordingDialog(
            initialPath = lastRecordedPath,
            onSave = { newName ->
                val file = File(lastRecordedPath)
                val newFile = File(file.parent, "$newName.wav")
                if (file.exists()) {
                    if (file.renameTo(newFile)) {
                        Toast.makeText(context, "Guardado: ${newFile.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al renombrar", Toast.LENGTH_SHORT).show()
                    }
                }
                showSaveDialog = false
            },
            onDiscard = {
                val file = File(lastRecordedPath)
                if (file.exists()) {
                    file.delete()
                    Toast.makeText(context, "Grabación eliminada", Toast.LENGTH_SHORT).show()
                }
                showSaveDialog = false
            },
            onDismiss = {
                // Just close dialog, keep auto-generated name
                showSaveDialog = false
                Toast.makeText(context, "Guardado automáticamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
             if (isRunning) {
                 BottomAppBar {
                     Row(
                         modifier = Modifier.fillMaxWidth().padding(16.dp),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Text(
                             text = if (isRecording) "REC: ${formatTime(recordingSeconds)}" else "Listo",
                             color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                         )
                         
                         IconButton(onClick = { isRecording = !isRecording }) {
                             Icon(
                                 imageVector = if (isRecording) SvgIcons.Stop else SvgIcons.Record,
                                 contentDescription = "Grabar",
                                 tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                             )
                         }
                     }
                 }
             }
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Estudio de Audio", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Main Workspace with Cable
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        canvasPosition = coordinates.positionInRoot()
                    }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Input Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(end = 8.dp)
                    ) {
                        Text("Entradas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        inputDevices.forEach { device ->
                            val isSelected = device.id == selectedInputDevice?.id
                            DeviceItem(
                                name = "${device.productName}\n(${typeToString(device.type)})",
                                isSelected = isSelected,
                                isInput = true,
                                onClick = {
                                    if (!isRunning) selectedInputDevice = device
                                    else Toast.makeText(context, "Detenga para cambiar", Toast.LENGTH_SHORT).show()
                                },
                                onPositioned = { offset ->
                                    if (isSelected) inputPoint = offset
                                }
                            )
                        }
                    }

                    // Output Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 8.dp)
                    ) {
                        Text("Salidas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        outputDevices.forEach { device ->
                            val isSelected = device.id == selectedOutputDevice?.id
                            DeviceItem(
                                name = "${device.productName}\n(${typeToString(device.type)})",
                                isSelected = isSelected,
                                isInput = false,
                                onClick = {
                                    if (!isRunning) selectedOutputDevice = device
                                    else Toast.makeText(context, "Detenga para cambiar", Toast.LENGTH_SHORT).show()
                                },
                                onPositioned = { offset ->
                                    if (isSelected) outputPoint = offset
                                }
                            )
                        }
                    }
                }

                // Draw Cable
                if (inputPoint != Offset.Unspecified && outputPoint != Offset.Unspecified) {
                    val lineColor = Color(201,83,46)
                    Canvas(modifier = Modifier.fillMaxSize().pointerInputPassThrough()) {
                        val start = inputPoint - canvasPosition
                        val end = outputPoint - canvasPosition
                        
                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(
                                start.x + 100f, start.y,
                                end.x - 100f, end.y,
                                end.x, end.y
                            )
                        }
                        
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.3f),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Ganancia: ${(volume * 100).toInt()}%")
            Slider(value = volume, onValueChange = { volume = it }, valueRange = 0f..5f, steps = 9)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isRunning) "APAGAR SISTEMA" else "ENCENDER SISTEMA")
            }
        }
    }
}

@Composable
fun SaveRecordingDialog(
    initialPath: String,
    onSave: (String) -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    val file = File(initialPath)
    var filename by remember { mutableStateOf(file.nameWithoutExtension) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grabación Finalizada") },
        text = {
            Column {
                Text("Ruta: ${file.parent}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text("Nombre del archivo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(filename) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDiscard) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}

@Composable
fun DeviceItem(
    name: String, 
    isSelected: Boolean, 
    isInput: Boolean,
    onClick: () -> Unit,
    onPositioned: (Offset) -> Unit
) {
    var myPosition by remember { mutableStateOf(Offset.Zero) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .onGloballyPositioned { coordinates ->
                // Get center of the connection edge
                val size = coordinates.size
                val position = coordinates.positionInRoot()
                val x = if (isInput) position.x + size.width else position.x
                val y = position.y + size.height / 2f
                onPositioned(Offset(x, y))
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isInput) Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (isInput) Spacer(modifier = Modifier.width(8.dp))
            
            // Visual Connector Port
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

// Helper Modifier to pass through clicks on Canvas if it overlaps
fun Modifier.pointerInputPassThrough(): Modifier = this // Canvas usually doesn't block clicks unless it has pointerInput. Box z-index matters.

object SvgIcons {
    val Record = ImageVector.Builder(
        name = "Record",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = androidx.compose.ui.graphics.SolidColor(Color.Red)) {
        moveTo(12f, 12f)
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
    }.build()

    val Stop = ImageVector.Builder(
        name = "Stop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(fill = androidx.compose.ui.graphics.SolidColor(Color.Black)) {
        moveTo(6f, 6f)
        lineTo(18f, 6f)
        lineTo(18f, 18f)
        lineTo(6f, 18f)
        close()
    }.build()
}

// ... existing AudioEngine and helper functions ...
fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

fun typeToString(type: Int): String {
    return when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Mic Interno"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Altavoz"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Headset"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Headphones"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB Audio"
        else -> "Otro"
    }
}

class AudioEngine(
    val context: Context,
    val inputDevice: AudioDeviceInfo,
    val outputDevice: AudioDeviceInfo,
    val getVolume: () -> Float,
    val onRecordingError: (String) -> Unit,
    val onRecordingSaved: (String) -> Unit,
    val onRecordingProgress: (Long) -> Unit
) {
    var onRecordingSavedUI: ((String) -> Unit)? = null
    
    private val isRunning = AtomicBoolean(false)
    private val isRecording = AtomicBoolean(false)
    private var audioThread: Thread? = null
    private var recordingThread: Thread? = null
    private val recordingQueue = ConcurrentLinkedQueue<ShortArray>()

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        
        audioThread = thread(start = true) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            runLoop()
        }
        
        recordingThread = thread(start = true) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            runRecordingLoop()
        }
    }

    fun stop() {
        isRunning.set(false)
        isRecording.set(false)
        try {
            audioThread?.join(1000)
            recordingThread?.join(1000)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun setRecording(record: Boolean) {
        isRecording.set(record)
    }

    private fun runLoop() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        val sampleRate = sampleRateStr?.toIntOrNull() ?: 48000
        
        val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
        val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        
        val minBufIn = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)
        val minBufOut = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)
        
        var record: AudioRecord? = null
        var track: AudioTrack? = null

        try {
            val audioSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaRecorder.AudioSource.UNPROCESSED
            } else {
                MediaRecorder.AudioSource.MIC
            }

            record = AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(audioFormat).setSampleRate(sampleRate).setChannelMask(channelConfigIn).build())
                .setBufferSizeInBytes(maxOf(minBufIn, 1024)) 
                .build()
            record.preferredDevice = inputDevice

            val trackBuilder = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(audioFormat).setSampleRate(sampleRate).setChannelMask(channelConfigOut).build())
                .setBufferSizeInBytes(maxOf(minBufOut, 1024))
                .setTransferMode(AudioTrack.MODE_STREAM)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            }
            
            track = trackBuilder.build()
            track.preferredDevice = outputDevice

            val chunkSize = minBufIn / 2
            val chunkBuffer = ShortArray(chunkSize)

            record.startRecording()
            track.play()

            while (isRunning.get()) {
                val read = record.read(chunkBuffer, 0, chunkBuffer.size)
                if (read > 0) {
                    val vol = getVolume()
                    if (vol != 1.0f) {
                        for (i in 0 until read) {
                            var s = (chunkBuffer[i] * vol).toInt()
                            if (s > Short.MAX_VALUE) s = Short.MAX_VALUE.toInt()
                            if (s < Short.MIN_VALUE) s = Short.MIN_VALUE.toInt()
                            chunkBuffer[i] = s.toShort()
                        }
                    }
                    track.write(chunkBuffer, 0, read)
                    
                    if (isRecording.get()) {
                        recordingQueue.offer(chunkBuffer.copyOfRange(0, read))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { track?.stop(); track?.release() } catch (e: Exception) {}
            try { record?.stop(); record?.release() } catch (e: Exception) {}
        }
    }

    private fun runRecordingLoop() {
        var fos: FileOutputStream? = null
        var currentFile: File? = null
        var payloadSize = 0L
        var startTime = 0L
        
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 48000

        while (isRunning.get()) {
            if (isRecording.get() && fos == null) {
                try {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC)
                    currentFile = File(dir, "MicAmp_$timeStamp.wav")
                    fos = FileOutputStream(currentFile)
                    writeWavHeader(fos!!, sampleRate, 0)
                    payloadSize = 0
                    startTime = System.currentTimeMillis()
                } catch (e: Exception) {
                    e.printStackTrace()
                    isRecording.set(false)
                }
            }

            if (!isRecording.get() && fos != null) {
                try {
                    fos.close()
                    updateWavHeader(currentFile!!, payloadSize.toInt(), sampleRate)
                    
                    val path = currentFile.absolutePath
                    withContextUI { onRecordingSavedUI?.invoke(path) }
                    
                    fos = null
                    currentFile = null
                    payloadSize = 0
                } catch (e: Exception) { e.printStackTrace() }
            }
            
            val chunk = recordingQueue.poll()
            if (chunk != null && fos != null) {
                try {
                    val byteBuffer = ByteBuffer.allocate(chunk.size * 2)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    for (s in chunk) byteBuffer.putShort(s)
                    fos.write(byteBuffer.array())
                    payloadSize += chunk.size * 2
                    
                    val elapsed = (System.currentTimeMillis() - startTime) / 1000
                    withContextUI { onRecordingProgress(elapsed) }
                    
                } catch (e: Exception) { e.printStackTrace() }
            } else {
                if (chunk == null) {
                    try { Thread.sleep(2) } catch (e: InterruptedException) {}
                }
            }
        }
        
        if (fos != null) {
             try {
                fos.close()
                updateWavHeader(currentFile!!, payloadSize.toInt(), sampleRate)
             } catch (e: Exception) {}
        }
    }
    
    private fun withContextUI(block: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(block)
    }

    private fun writeWavHeader(out: FileOutputStream, sampleRate: Int, totalAudioLen: Int) {
         val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * 2 * channels
        
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // Format PCM
        header[22] = channels.toByte(); header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (2 * channels).toByte(); header[33] = 0 // Block align
        header[34] = 16; header[35] = 0 // Bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()
        
        out.write(header, 0, 44)
    }

    private fun updateWavHeader(file: File, totalAudioLen: Int, sampleRate: Int) {
        try {
            val raf = RandomAccessFile(file, "rw")
            raf.seek(0)
            val totalDataLen = totalAudioLen + 36
            raf.skipBytes(4)
            raf.write(intToByteArray(totalDataLen), 0, 4)
            raf.seek(40)
            raf.write(intToByteArray(totalAudioLen), 0, 4)
            raf.close()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun intToByteArray(data: Int): ByteArray {
        return byteArrayOf(
            (data and 0xff).toByte(),
            ((data shr 8) and 0xff).toByte(),
            ((data shr 16) and 0xff).toByte(),
            ((data shr 24) and 0xff).toByte()
        )
    }
}
