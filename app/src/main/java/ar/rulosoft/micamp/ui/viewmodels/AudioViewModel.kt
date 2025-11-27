package ar.rulosoft.micamp.ui.viewmodels

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ar.rulosoft.micamp.DPS.AudioEngine
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.io.File

class AudioViewModel(private val context: Context) : ViewModel() {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = context.getSharedPreferences("MicAmpPrefs", Context.MODE_PRIVATE)

    // Device State
    var inputDevices by mutableStateOf(listOf<AudioDeviceInfo>())
    var outputDevices by mutableStateOf(listOf<AudioDeviceInfo>())
    var selectedInputDevice by mutableStateOf<AudioDeviceInfo?>(null)
    var selectedOutputDevice by mutableStateOf<AudioDeviceInfo?>(null)

    // Engine State
    var isRunning by mutableStateOf(false)
    var isRecording by mutableStateOf(false)
    var recordingSeconds by mutableLongStateOf(0L)
    var visualizerData by mutableStateOf(FloatArray(50))
    private var engine: AudioEngine? = null

    // DSP State
    var volume by mutableFloatStateOf(1.0f)
    
    // Distortion
    var distortion by mutableFloatStateOf(0.0f)
    var isDistortionEnabled by mutableStateOf(false)

    // EQ
    var isEqEnabled by mutableStateOf(true)
    val eqBands = mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f)
    
    // Delay
    var isDelayEnabled by mutableStateOf(false)
    var delayTime by mutableFloatStateOf(0.3f)
    var delayFeedback by mutableFloatStateOf(0.4f)
    var delayMix by mutableFloatStateOf(0.3f)
    
    // Reverb
    var isReverbEnabled by mutableStateOf(false)
    var reverbMix by mutableFloatStateOf(0.3f)
    var reverbSize by mutableFloatStateOf(0.5f)
    
    // Compressor
    var isCompressorEnabled by mutableStateOf(false)
    var compThreshold by mutableFloatStateOf(0.5f) // 0 to 1 (linear)
    var compRatio by mutableFloatStateOf(4.0f) // 1 to 20
    var compMakeup by mutableFloatStateOf(1.0f) // 1 to 4

    // Tremolo
    var isTremoloEnabled by mutableStateOf(false)
    var tremDepth by mutableFloatStateOf(0.5f) // 0 to 1
    var tremRate by mutableFloatStateOf(5.0f) // Hz (0.1 to 20)
    
    // Chorus
    var isChorusEnabled by mutableStateOf(false)
    var chorusRate by mutableFloatStateOf(1.0f) // Hz (0.1 to 5)
    var chorusDepth by mutableFloatStateOf(2.0f) // ms (1 to 10)
    var chorusMix by mutableFloatStateOf(0.5f) // 0 to 1

    // File/Recording State
    var currentSaveDir by mutableStateOf<Uri?>(null)
    var lastRecordedPath by mutableStateOf("")
    var showSaveDialog by mutableStateOf(false)

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
    }

    init {
        loadSettings()
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        updateDevices()

        // Auto-save settings when they change
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            snapshotFlow {
                // Construct a list of all state values we want to persist
                // This block runs whenever any read state object changes
                listOf(
                    volume, distortion, isDistortionEnabled, isEqEnabled, 
                    isDelayEnabled, delayTime, delayFeedback, delayMix,
                    isReverbEnabled, reverbMix, reverbSize,
                    isCompressorEnabled, compThreshold, compRatio, compMakeup,
                    isTremoloEnabled, tremDepth, tremRate,
                    isChorusEnabled, chorusRate, chorusDepth, chorusMix,
                    selectedInputDevice?.productName, selectedOutputDevice?.productName, currentSaveDir
                ) + eqBands.toList()
            }.debounce(500) // Debounce 500ms to avoid spamming SharedPreferences during slider drags
             .collectLatest {
                 saveSettings()
             }
        }
    }

    private fun loadSettings() {
        for (i in 0 until 6) {
            eqBands[i] = prefs.getFloat("eq_band_$i", 0f)
        }
        volume = prefs.getFloat("master_volume", 1.0f)

        isDistortionEnabled = prefs.getBoolean("distortion_enabled", false)
        distortion = prefs.getFloat("distortion_value", 0.0f)
        isEqEnabled = prefs.getBoolean("eq_enabled", true)
        isDelayEnabled = prefs.getBoolean("delay_enabled", false)
        delayTime = prefs.getFloat("delay_time", 0.3f)
        delayFeedback = prefs.getFloat("delay_feedback", 0.4f)
        delayMix = prefs.getFloat("delay_mix", 0.3f)
        
        // New params load
        isReverbEnabled = prefs.getBoolean("reverb_enabled", false)
        reverbMix = prefs.getFloat("reverb_mix", 0.3f)
        reverbSize = prefs.getFloat("reverb_size", 0.5f)
        
        isCompressorEnabled = prefs.getBoolean("comp_enabled", false)
        compThreshold = prefs.getFloat("comp_threshold", 0.5f)
        compRatio = prefs.getFloat("comp_ratio", 4.0f)
        compMakeup = prefs.getFloat("comp_makeup", 1.0f)
        
        isTremoloEnabled = prefs.getBoolean("trem_enabled", false)
        tremDepth = prefs.getFloat("trem_depth", 0.5f)
        tremRate = prefs.getFloat("trem_rate", 5.0f)
        
        isChorusEnabled = prefs.getBoolean("chorus_enabled", false)
        chorusRate = prefs.getFloat("chorus_rate", 1.0f)
        chorusDepth = prefs.getFloat("chorus_depth", 2.0f)
        chorusMix = prefs.getFloat("chorus_mix", 0.5f)
        
        val savedUri = prefs.getString("save_dir_uri", null)
        if (savedUri != null) {
            currentSaveDir = Uri.parse(savedUri)
        }
    }

    fun saveSettings() {
        val editor = prefs.edit()
        eqBands.forEachIndexed { index, gain -> editor.putFloat("eq_band_$index", gain) }
        editor.putFloat("master_volume", volume)

        editor.putBoolean("distortion_enabled", isDistortionEnabled)
        editor.putFloat("distortion_value", distortion)
        editor.putBoolean("eq_enabled", isEqEnabled)
        editor.putBoolean("delay_enabled", isDelayEnabled)
        editor.putFloat("delay_time", delayTime)
        editor.putFloat("delay_feedback", delayFeedback)
        editor.putFloat("delay_mix", delayMix)
        
        // New params save
        editor.putBoolean("reverb_enabled", isReverbEnabled)
        editor.putFloat("reverb_mix", reverbMix)
        editor.putFloat("reverb_size", reverbSize)
        
        editor.putBoolean("comp_enabled", isCompressorEnabled)
        editor.putFloat("comp_threshold", compThreshold)
        editor.putFloat("comp_ratio", compRatio)
        editor.putFloat("comp_makeup", compMakeup)
        
        editor.putBoolean("trem_enabled", isTremoloEnabled)
        editor.putFloat("trem_depth", tremDepth)
        editor.putFloat("trem_rate", tremRate)
        
        editor.putBoolean("chorus_enabled", isChorusEnabled)
        editor.putFloat("chorus_rate", chorusRate)
        editor.putFloat("chorus_depth", chorusDepth)
        editor.putFloat("chorus_mix", chorusMix)
        
        selectedInputDevice?.let { editor.putString("last_input_name", it.productName.toString()) }
        selectedOutputDevice?.let { editor.putString("last_output_name", it.productName.toString()) }
        
        currentSaveDir?.let { editor.putString("save_dir_uri", it.toString()) }
        
        editor.apply()
    }

    fun updateDevices() {
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList()
        inputDevices = inputs
        
        if (selectedInputDevice == null) {
            val savedInputName = prefs.getString("last_input_name", null)
            if (savedInputName != null) {
                selectedInputDevice = inputs.find { it.productName == savedInputName }
            }
            if (selectedInputDevice == null || !inputs.any { it.id == selectedInputDevice?.id }) {
                selectedInputDevice = inputs.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC } ?: inputs.firstOrNull()
            }
        } else {
             // Check if current device is still valid
             val current = inputs.find { it.id == selectedInputDevice?.id }
             if (current != null) {
                 selectedInputDevice = current
             } else {
                 // Device disconnected?
                 // Try to recover by name or default
                 val savedInputName = prefs.getString("last_input_name", null)
                 selectedInputDevice = inputs.find { it.productName == savedInputName } ?: inputs.firstOrNull()
             }
        }

        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
        outputDevices = outputs
        
        if (selectedOutputDevice == null) {
            val savedOutputName = prefs.getString("last_output_name", null)
             if (savedOutputName != null) {
                selectedOutputDevice = outputs.find { it.productName == savedOutputName }
            }
            if (selectedOutputDevice == null || !outputs.any { it.id == selectedOutputDevice?.id }) {
                selectedOutputDevice = outputs.find { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER } ?: outputs.firstOrNull()
            }
        } else {
             // Check if current device is still valid
             val current = outputs.find { it.id == selectedOutputDevice?.id }
             if (current != null) {
                 selectedOutputDevice = current
             } else {
                 // Device disconnected?
                 val savedOutputName = prefs.getString("last_output_name", null)
                 selectedOutputDevice = outputs.find { it.productName == savedOutputName } ?: outputs.firstOrNull()
             }
        }
    }

    fun toggleEngine() {
        if (isRunning) {
            stopEngine()
        } else {
            startEngine()
        }
    }

    private fun startEngine() {
        if (selectedInputDevice != null && selectedOutputDevice != null) {
            stopEngine() // Ensure clean state
            engine = AudioEngine(
                context = context,
                inputDevice = selectedInputDevice!!,
                outputDevice = selectedOutputDevice!!,
                getVolume = { volume },
                getDistortion = { distortion },
                getEqGains = { eqBands.toFloatArray() },
                isDistortionEnabled = { isDistortionEnabled },
                isEqEnabled = { isEqEnabled },
                getDelayParams = { floatArrayOf(delayTime, delayFeedback, delayMix) },
                isDelayEnabled = { isDelayEnabled },
                
                getReverbParams = { floatArrayOf(reverbMix, reverbSize) },
                isReverbEnabled = { isReverbEnabled },
                getCompressorParams = { floatArrayOf(compThreshold, compRatio, compMakeup) },
                isCompressorEnabled = { isCompressorEnabled },
                getTremoloParams = { floatArrayOf(tremDepth, tremRate) },
                isTremoloEnabled = { isTremoloEnabled },
                getChorusParams = { floatArrayOf(chorusRate, chorusDepth, chorusMix) },
                isChorusEnabled = { isChorusEnabled },
                
                onRecordingError = { isRecording = false },
                onRecordingSaved = { },
                onRecordingProgress = { seconds -> recordingSeconds = seconds },
                onVisualizerData = { data -> visualizerData = data },
                saveDir = currentSaveDir
            )
            engine?.onRecordingSavedUI = { path ->
                lastRecordedPath = path
                showSaveDialog = true
            }
            engine?.start()
            engine?.setRecording(isRecording)
            isRunning = true
        }
    }

    private fun stopEngine() {
        engine?.stop()
        engine = null
        isRunning = false
        visualizerData = FloatArray(50) // Reset visualizer
    }

    fun toggleRecording() {
        isRecording = !isRecording
        if (isRecording) recordingSeconds = 0
        engine?.setRecording(isRecording)
    }

    fun setSaveDirectory(uri: Uri) {
        currentSaveDir = uri
        saveSettings()
    }

    override fun onCleared() {
        super.onCleared()
        stopEngine()
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        saveSettings()
    }
}

class AudioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
