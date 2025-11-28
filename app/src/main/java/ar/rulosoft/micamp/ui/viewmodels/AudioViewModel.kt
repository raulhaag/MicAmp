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
import ar.rulosoft.micamp.data.DspConfig
import ar.rulosoft.micamp.data.EffectType
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.io.File

class AudioViewModel(private val context: Context) : ViewModel() {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = context.getSharedPreferences("MicAmpPrefs", Context.MODE_PRIVATE)
    private val presetsPrefs = context.getSharedPreferences("MicAmpPresets", Context.MODE_PRIVATE)

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
    
    // Effect Order (Signal Chain)
    // Default order
    val effectOrder = mutableStateListOf(
        EffectType.NOISE_GATE,
        EffectType.COMPRESSOR,
        EffectType.AUTO_WAH,
        EffectType.BITCRUSHER,
        EffectType.EQ,
        EffectType.DISTORTION,
        EffectType.PHASER,
        EffectType.FLANGER,
        EffectType.TREMOLO,
        EffectType.CHORUS,
        EffectType.DELAY,
        EffectType.REVERB,
        EffectType.LIMITER
    )

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

    // Noise Gate
    var isNoiseGateEnabled by mutableStateOf(false)
    var noiseGateThreshold by mutableFloatStateOf(0.05f) // 0 to 1

    // Flanger
    var isFlangerEnabled by mutableStateOf(false)
    var flangerRate by mutableFloatStateOf(0.5f) // Hz (0.1 to 2)
    var flangerDepth by mutableFloatStateOf(2.0f) // ms (1 to 5)
    var flangerFeedback by mutableFloatStateOf(0.5f) // 0 to 0.9
    var flangerMix by mutableFloatStateOf(0.5f) // 0 to 1
    
    // Phaser
    var isPhaserEnabled by mutableStateOf(false)
    var phaserRate by mutableFloatStateOf(1.0f) // Hz (0.1 to 10)
    var phaserDepth by mutableFloatStateOf(0.5f) // 0 to 1
    var phaserFeedback by mutableFloatStateOf(0.5f) // 0 to 0.9
    var phaserMix by mutableFloatStateOf(0.5f) // 0 to 1

    // Bitcrusher
    var isBitcrusherEnabled by mutableStateOf(false)
    var bitcrusherDepth by mutableFloatStateOf(0.5f) // 0 to 1 (16 to 1 bit)
    var bitcrusherRate by mutableFloatStateOf(0.1f) // 0 to 1 (sample rate reduction)
    var bitcrusherMix by mutableFloatStateOf(1.0f) // 0 to 1
    
    // Limiter
    var isLimiterEnabled by mutableStateOf(false)
    var limiterThreshold by mutableFloatStateOf(0.95f) // 0 to 1
    
    // AutoWah
    var isAutoWahEnabled by mutableStateOf(false)
    var autoWahDepth by mutableFloatStateOf(0.5f) // Range
    var autoWahRate by mutableFloatStateOf(0.5f) // Sensitivity
    var autoWahMix by mutableFloatStateOf(0.5f) // Mix
    var autoWahResonance by mutableFloatStateOf(0.5f) // Resonance

    // File/Recording State
    var currentSaveDir by mutableStateOf<Uri?>(null)
    var lastRecordedPath by mutableStateOf("")
    var showSaveDialog by mutableStateOf(false)
    
    // Presets State
    var presetList by mutableStateOf(listOf<String>())
    var showPresetsDialog by mutableStateOf(false)

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) = updateDevices()
    }

    init {
        loadSettings()
        loadPresetList()
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        updateDevices()

        // Auto-save settings when they change
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            snapshotFlow {
                // Construct a list of all state values we want to persist
                listOf(
                    volume, distortion, isDistortionEnabled, isEqEnabled, 
                    isDelayEnabled, delayTime, delayFeedback, delayMix,
                    isReverbEnabled, reverbMix, reverbSize,
                    isCompressorEnabled, compThreshold, compRatio, compMakeup,
                    isTremoloEnabled, tremDepth, tremRate,
                    isChorusEnabled, chorusRate, chorusDepth, chorusMix,
                    isNoiseGateEnabled, noiseGateThreshold,
                    isFlangerEnabled, flangerRate, flangerDepth, flangerFeedback, flangerMix,
                    isPhaserEnabled, phaserRate, phaserDepth, phaserFeedback, phaserMix,
                    isBitcrusherEnabled, bitcrusherDepth, bitcrusherRate, bitcrusherMix,
                    isLimiterEnabled, limiterThreshold,
                    isAutoWahEnabled, autoWahDepth, autoWahRate, autoWahMix, autoWahResonance,
                    selectedInputDevice?.productName, selectedOutputDevice?.productName, currentSaveDir
                ) + eqBands.toList() + effectOrder.toList()
            }.debounce(500) // Debounce 500ms
             .collectLatest {
                 saveSettings()
             }
        }
    }
    
    private fun loadPresetList() {
        presetList = presetsPrefs.all.keys.sorted()
    }
    
    fun savePreset(name: String) {
        val config = DspConfig(
            name = name,
            volume = volume,
            distortion = distortion, isDistortionEnabled = isDistortionEnabled,
            eqBands = eqBands.toList(), isEqEnabled = isEqEnabled,
            delayTime = delayTime, delayFeedback = delayFeedback, delayMix = delayMix, isDelayEnabled = isDelayEnabled,
            reverbMix = reverbMix, reverbSize = reverbSize, isReverbEnabled = isReverbEnabled,
            compThreshold = compThreshold, compRatio = compRatio, compMakeup = compMakeup, isCompressorEnabled = isCompressorEnabled,
            tremDepth = tremDepth, tremRate = tremRate, isTremoloEnabled = isTremoloEnabled,
            chorusRate = chorusRate, chorusDepth = chorusDepth, chorusMix = chorusMix, isChorusEnabled = isChorusEnabled,
            
            noiseGateThreshold = noiseGateThreshold, isNoiseGateEnabled = isNoiseGateEnabled,
            
            flangerRate = flangerRate, flangerDepth = flangerDepth, flangerMix = flangerMix, flangerFeedback = flangerFeedback, isFlangerEnabled = isFlangerEnabled,
            
            phaserRate = phaserRate, phaserDepth = phaserDepth, phaserMix = phaserMix, phaserFeedback = phaserFeedback, isPhaserEnabled = isPhaserEnabled,
            
            bitcrusherDepth = bitcrusherDepth, bitcrusherRate = bitcrusherRate, bitcrusherMix = bitcrusherMix, isBitcrusherEnabled = isBitcrusherEnabled,
            
            limiterThreshold = limiterThreshold, isLimiterEnabled = isLimiterEnabled,
            
            autoWahDepth = autoWahDepth, autoWahRate = autoWahRate, autoWahMix = autoWahMix, autoWahResonance = autoWahResonance, isAutoWahEnabled = isAutoWahEnabled,
            
            effectOrder = effectOrder.toList()
        )
        presetsPrefs.edit().putString(name, config.toJson()).apply()
        loadPresetList()
    }
    
    fun loadPreset(name: String) {
        val json = presetsPrefs.getString(name, null) ?: return
        try {
            val config = DspConfig.fromJson(json)
            
            // Apply Order first? Or just apply params
            if (config.effectOrder.isNotEmpty()) {
                effectOrder.clear()
                effectOrder.addAll(config.effectOrder)
            }
            
            // Apply
            volume = config.volume
            
            distortion = config.distortion
            isDistortionEnabled = config.isDistortionEnabled
            
            isEqEnabled = config.isEqEnabled
            for(i in 0 until 6) {
                if (i < config.eqBands.size) eqBands[i] = config.eqBands[i]
            }
            
            delayTime = config.delayTime
            delayFeedback = config.delayFeedback
            delayMix = config.delayMix
            isDelayEnabled = config.isDelayEnabled
            
            reverbMix = config.reverbMix
            reverbSize = config.reverbSize
            isReverbEnabled = config.isReverbEnabled
            
            compThreshold = config.compThreshold
            compRatio = config.compRatio
            compMakeup = config.compMakeup
            isCompressorEnabled = config.isCompressorEnabled
            
            tremDepth = config.tremDepth
            tremRate = config.tremRate
            isTremoloEnabled = config.isTremoloEnabled
            
            chorusRate = config.chorusRate
            chorusDepth = config.chorusDepth
            chorusMix = config.chorusMix
            isChorusEnabled = config.isChorusEnabled
            
            noiseGateThreshold = config.noiseGateThreshold
            isNoiseGateEnabled = config.isNoiseGateEnabled
            
            flangerRate = config.flangerRate
            flangerDepth = config.flangerDepth
            flangerMix = config.flangerMix
            flangerFeedback = config.flangerFeedback
            isFlangerEnabled = config.isFlangerEnabled
            
            phaserRate = config.phaserRate
            phaserDepth = config.phaserDepth
            phaserMix = config.phaserMix
            phaserFeedback = config.phaserFeedback
            isPhaserEnabled = config.isPhaserEnabled
            
            bitcrusherDepth = config.bitcrusherDepth
            bitcrusherRate = config.bitcrusherRate
            bitcrusherMix = config.bitcrusherMix
            isBitcrusherEnabled = config.isBitcrusherEnabled
            
            limiterThreshold = config.limiterThreshold
            isLimiterEnabled = config.isLimiterEnabled
            
            autoWahDepth = config.autoWahDepth
            autoWahRate = config.autoWahRate
            autoWahMix = config.autoWahMix
            autoWahResonance = config.autoWahResonance
            isAutoWahEnabled = config.isAutoWahEnabled
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun deletePreset(name: String) {
        presetsPrefs.edit().remove(name).apply()
        loadPresetList()
    }

    private fun loadSettings() {
        for (i in 0 until 6) {
            eqBands[i] = prefs.getFloat("eq_band_$i", 0f)
        }
        volume = prefs.getFloat("master_volume", 1.0f)
        
        // Load Order
        val orderStr = prefs.getString("effect_order", null)
        if (orderStr != null) {
            try {
                val list = orderStr.split(",").mapNotNull { 
                    try { EffectType.valueOf(it) } catch(e: Exception) { null } 
                }
                if (list.isNotEmpty()) {
                    // Ensure we have all effects, append missing ones at end
                    val currentSet = list.toSet()
                    val missing = EffectType.values().filter { !currentSet.contains(it) }
                    effectOrder.clear()
                    effectOrder.addAll(list + missing)
                }
            } catch(e: Exception) { e.printStackTrace() }
        }

        isDistortionEnabled = prefs.getBoolean("distortion_enabled", false)
        distortion = prefs.getFloat("distortion_value", 0.0f)
        isEqEnabled = prefs.getBoolean("eq_enabled", true)
        isDelayEnabled = prefs.getBoolean("delay_enabled", false)
        delayTime = prefs.getFloat("delay_time", 0.3f)
        delayFeedback = prefs.getFloat("delay_feedback", 0.4f)
        delayMix = prefs.getFloat("delay_mix", 0.3f)
        
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
        
        isNoiseGateEnabled = prefs.getBoolean("noisegate_enabled", false)
        noiseGateThreshold = prefs.getFloat("noisegate_thresh", 0.05f)
        
        isFlangerEnabled = prefs.getBoolean("flanger_enabled", false)
        flangerRate = prefs.getFloat("flanger_rate", 0.5f)
        flangerDepth = prefs.getFloat("flanger_depth", 2.0f)
        flangerFeedback = prefs.getFloat("flanger_feedback", 0.5f)
        flangerMix = prefs.getFloat("flanger_mix", 0.5f)
        
        isPhaserEnabled = prefs.getBoolean("phaser_enabled", false)
        phaserRate = prefs.getFloat("phaser_rate", 1.0f)
        phaserDepth = prefs.getFloat("phaser_depth", 0.5f)
        phaserFeedback = prefs.getFloat("phaser_feedback", 0.5f)
        phaserMix = prefs.getFloat("phaser_mix", 0.5f)
        
        isBitcrusherEnabled = prefs.getBoolean("bitcrusher_enabled", false)
        bitcrusherDepth = prefs.getFloat("bitcrusher_depth", 0.5f)
        bitcrusherRate = prefs.getFloat("bitcrusher_rate", 0.1f)
        bitcrusherMix = prefs.getFloat("bitcrusher_mix", 1.0f)
        
        isLimiterEnabled = prefs.getBoolean("limiter_enabled", false)
        limiterThreshold = prefs.getFloat("limiter_thresh", 0.95f)
        
        isAutoWahEnabled = prefs.getBoolean("autowah_enabled", false)
        autoWahDepth = prefs.getFloat("autowah_depth", 0.5f)
        autoWahRate = prefs.getFloat("autowah_rate", 0.5f)
        autoWahMix = prefs.getFloat("autowah_mix", 0.5f)
        autoWahResonance = prefs.getFloat("autowah_resonance", 0.5f)
        
        val savedUri = prefs.getString("save_dir_uri", null)
        if (savedUri != null) {
            currentSaveDir = Uri.parse(savedUri)
        }
    }

    fun saveSettings() {
        val editor = prefs.edit()
        eqBands.forEachIndexed { index, gain -> editor.putFloat("eq_band_$index", gain) }
        editor.putFloat("master_volume", volume)
        
        // Save Order
        editor.putString("effect_order", effectOrder.joinToString(",") { it.name })

        editor.putBoolean("distortion_enabled", isDistortionEnabled)
        editor.putFloat("distortion_value", distortion)
        editor.putBoolean("eq_enabled", isEqEnabled)
        editor.putBoolean("delay_enabled", isDelayEnabled)
        editor.putFloat("delay_time", delayTime)
        editor.putFloat("delay_feedback", delayFeedback)
        editor.putFloat("delay_mix", delayMix)
        
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
        
        editor.putBoolean("noisegate_enabled", isNoiseGateEnabled)
        editor.putFloat("noisegate_thresh", noiseGateThreshold)
        
        editor.putBoolean("flanger_enabled", isFlangerEnabled)
        editor.putFloat("flanger_rate", flangerRate)
        editor.putFloat("flanger_depth", flangerDepth)
        editor.putFloat("flanger_feedback", flangerFeedback)
        editor.putFloat("flanger_mix", flangerMix)
        
        editor.putBoolean("phaser_enabled", isPhaserEnabled)
        editor.putFloat("phaser_rate", phaserRate)
        editor.putFloat("phaser_depth", phaserDepth)
        editor.putFloat("phaser_feedback", phaserFeedback)
        editor.putFloat("phaser_mix", phaserMix)
        
        editor.putBoolean("bitcrusher_enabled", isBitcrusherEnabled)
        editor.putFloat("bitcrusher_depth", bitcrusherDepth)
        editor.putFloat("bitcrusher_rate", bitcrusherRate)
        editor.putFloat("bitcrusher_mix", bitcrusherMix)
        
        editor.putBoolean("limiter_enabled", isLimiterEnabled)
        editor.putFloat("limiter_thresh", limiterThreshold)
        
        editor.putBoolean("autowah_enabled", isAutoWahEnabled)
        editor.putFloat("autowah_depth", autoWahDepth)
        editor.putFloat("autowah_rate", autoWahRate)
        editor.putFloat("autowah_mix", autoWahMix)
        editor.putFloat("autowah_resonance", autoWahResonance)
        
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
             val current = inputs.find { it.id == selectedInputDevice?.id }
             if (current != null) {
                 selectedInputDevice = current
             } else {
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
             val current = outputs.find { it.id == selectedOutputDevice?.id }
             if (current != null) {
                 selectedOutputDevice = current
             } else {
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
                
                getEffectOrder = { effectOrder.toList() },
                
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
                
                getNoiseGateThreshold = { noiseGateThreshold },
                isNoiseGateEnabled = { isNoiseGateEnabled },
                
                getFlangerParams = { floatArrayOf(flangerRate, flangerDepth, flangerMix, flangerFeedback) },
                isFlangerEnabled = { isFlangerEnabled },
                
                getPhaserParams = { floatArrayOf(phaserRate, phaserDepth, phaserMix, phaserFeedback) },
                isPhaserEnabled = { isPhaserEnabled },
                
                getBitcrusherParams = { floatArrayOf(bitcrusherDepth, bitcrusherRate, bitcrusherMix) },
                isBitcrusherEnabled = { isBitcrusherEnabled },
                
                getLimiterThreshold = { limiterThreshold },
                isLimiterEnabled = { isLimiterEnabled },
                
                getAutoWahParams = { floatArrayOf(autoWahDepth, autoWahRate, autoWahMix, autoWahResonance) },
                isAutoWahEnabled = { isAutoWahEnabled },
                
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
    
    // Moves the effect to the end of the chain if enabling
    fun updateEffectStatus(effect: EffectType, isEnabled: Boolean) {
        if (isEnabled) {
            // Move to end of chain (highest priority/last processed)
            effectOrder.remove(effect)
            effectOrder.add(effect)
        }
        
        // Update the boolean state
        when (effect) {
            EffectType.NOISE_GATE -> isNoiseGateEnabled = isEnabled
            EffectType.COMPRESSOR -> isCompressorEnabled = isEnabled
            EffectType.AUTO_WAH -> isAutoWahEnabled = isEnabled
            EffectType.BITCRUSHER -> isBitcrusherEnabled = isEnabled
            EffectType.EQ -> isEqEnabled = isEnabled
            EffectType.DISTORTION -> isDistortionEnabled = isEnabled
            EffectType.PHASER -> isPhaserEnabled = isEnabled
            EffectType.FLANGER -> isFlangerEnabled = isEnabled
            EffectType.TREMOLO -> isTremoloEnabled = isEnabled
            EffectType.CHORUS -> isChorusEnabled = isEnabled
            EffectType.DELAY -> isDelayEnabled = isEnabled
            EffectType.REVERB -> isReverbEnabled = isEnabled
            EffectType.LIMITER -> isLimiterEnabled = isEnabled
        }
    }
    
    // Get the index (1-based) of the effect in the active chain
    // Returns 0 if not active
    fun getActiveEffectIndex(effect: EffectType): Int {
        // Filter only enabled effects from the master order list
        val activeEffects = effectOrder.filter { 
            when(it) {
                EffectType.NOISE_GATE -> isNoiseGateEnabled
                EffectType.COMPRESSOR -> isCompressorEnabled
                EffectType.AUTO_WAH -> isAutoWahEnabled
                EffectType.BITCRUSHER -> isBitcrusherEnabled
                EffectType.EQ -> isEqEnabled
                EffectType.DISTORTION -> isDistortionEnabled
                EffectType.PHASER -> isPhaserEnabled
                EffectType.FLANGER -> isFlangerEnabled
                EffectType.TREMOLO -> isTremoloEnabled
                EffectType.CHORUS -> isChorusEnabled
                EffectType.DELAY -> isDelayEnabled
                EffectType.REVERB -> isReverbEnabled
                EffectType.LIMITER -> isLimiterEnabled
            }
        }
        
        val index = activeEffects.indexOf(effect)
        return if (index >= 0) index + 1 else 0
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
