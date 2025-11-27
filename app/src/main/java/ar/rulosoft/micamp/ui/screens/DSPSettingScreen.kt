import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ar.rulosoft.micamp.ui.controls.EQ
import ar.rulosoft.micamp.ui.screens.ConfigCard
import ar.rulosoft.micamp.ui.screens.DspEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DspSettingsScreen(
    effect: DspEffect,
    onBack: () -> Unit,

    // Distortion
    distortion: Float = 0f,
    onDistortionChange: (Float) -> Unit = {},

    // EQ
    eqBands: MutableList<Float> = mutableListOf(),
    bandLabels: List<String> = emptyList(),

    // Delay
    delayTime: Float = 0f,
    onDelayTimeChange: (Float) -> Unit = {},
    delayFeedback: Float = 0f,
    onDelayFeedbackChange: (Float) -> Unit = {},
    delayMix: Float = 0f,
    onDelayMixChange: (Float) -> Unit = {},

    // Reverb
    reverbMix: Float = 0f,
    onReverbMixChange: (Float) -> Unit = {},
    reverbSize: Float = 0f,
    onReverbSizeChange: (Float) -> Unit = {},

    // Compressor
    compThreshold: Float = 0f,
    onCompThresholdChange: (Float) -> Unit = {},
    compRatio: Float = 0f,
    onCompRatioChange: (Float) -> Unit = {},
    compMakeup: Float = 0f,
    onCompMakeupChange: (Float) -> Unit = {},

    // Tremolo
    tremDepth: Float = 0f,
    onTremDepthChange: (Float) -> Unit = {},
    tremRate: Float = 0f,
    onTremRateChange: (Float) -> Unit = {},

    // Chorus
    chorusRate: Float = 0f,
    onChorusRateChange: (Float) -> Unit = {},
    chorusDepth: Float = 0f,
    onChorusDepthChange: (Float) -> Unit = {},
    chorusMix: Float = 0f,
    onChorusMixChange: (Float) -> Unit = {},
    
    // Noise Gate
    noiseGateThreshold: Float = 0f,
    onNoiseGateThresholdChange: (Float) -> Unit = {},
    
    // Flanger
    flangerRate: Float = 0f,
    onFlangerRateChange: (Float) -> Unit = {},
    flangerDepth: Float = 0f,
    onFlangerDepthChange: (Float) -> Unit = {},
    flangerFeedback: Float = 0f,
    onFlangerFeedbackChange: (Float) -> Unit = {},
    flangerMix: Float = 0f,
    onFlangerMixChange: (Float) -> Unit = {},

    // Phaser
    phaserRate: Float = 0f,
    onPhaserRateChange: (Float) -> Unit = {},
    phaserDepth: Float = 0f,
    onPhaserDepthChange: (Float) -> Unit = {},
    phaserFeedback: Float = 0f,
    onPhaserFeedbackChange: (Float) -> Unit = {},
    phaserMix: Float = 0f,
    onPhaserMixChange: (Float) -> Unit = {},

    // Bitcrusher
    bitcrusherDepth: Float = 0f,
    onBitcrusherDepthChange: (Float) -> Unit = {},
    bitcrusherRate: Float = 0f,
    onBitcrusherRateChange: (Float) -> Unit = {},
    bitcrusherMix: Float = 0f,
    onBitcrusherMixChange: (Float) -> Unit = {},

    // Limiter
    limiterThreshold: Float = 0f,
    onLimiterThresholdChange: (Float) -> Unit = {},

    // AutoWah
    autoWahDepth: Float = 0f,
    onAutoWahDepthChange: (Float) -> Unit = {},
    autoWahRate: Float = 0f,
    onAutoWahRateChange: (Float) -> Unit = {},
    autoWahMix: Float = 0f,
    onAutoWahMixChange: (Float) -> Unit = {},
    autoWahResonance: Float = 0f,
    onAutoWahResonanceChange: (Float) -> Unit = {}
) {
    val title = when(effect) {
        DspEffect.DISTORTION -> "Distorsión"
        DspEffect.EQ -> "Ecualizador"
        DspEffect.DELAY -> "Delay"
        DspEffect.REVERB -> "Reverb"
        DspEffect.COMPRESSOR -> "Compresor"
        DspEffect.TREMOLO -> "Tremolo"
        DspEffect.CHORUS -> "Chorus"
        DspEffect.NOISE_GATE -> "Noise Gate"
        DspEffect.FLANGER -> "Flanger"
        DspEffect.PHASER -> "Phaser"
        DspEffect.BITCRUSHER -> "Bitcrusher"
        DspEffect.LIMITER -> "Limitador"
        DspEffect.AUTO_WAH -> "Auto Wah"
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when(effect) {
                DspEffect.AUTO_WAH -> {
                    ConfigCard(title = "Configuración: Auto Wah", color = Color(0xFFFFD600)) {
                        Text("Mix: ${(autoWahMix * 100).toInt()}%")
                        Slider(value = autoWahMix, onValueChange = onAutoWahMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD600), activeTrackColor = Color(0xFFFFD600)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sensibilidad: ${(autoWahRate * 100).toInt()}%")
                        Slider(value = autoWahRate, onValueChange = onAutoWahRateChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD600), activeTrackColor = Color(0xFFFFD600)))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rango (Depth): ${(autoWahDepth * 100).toInt()}%")
                        Slider(value = autoWahDepth, onValueChange = onAutoWahDepthChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD600), activeTrackColor = Color(0xFFFFD600)))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Resonancia: ${(autoWahResonance * 100).toInt()}%")
                        Slider(value = autoWahResonance, onValueChange = onAutoWahResonanceChange, valueRange = 0f..0.95f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD600), activeTrackColor = Color(0xFFFFD600)))
                    }
                }
                DspEffect.LIMITER -> {
                    ConfigCard(title = "Configuración: Limitador", color = Color(0xFFE65100)) {
                        Text("Techo (Ceiling): ${String.format("%.2f", limiterThreshold)}")
                        Slider(value = limiterThreshold, onValueChange = onLimiterThresholdChange, valueRange = 0.1f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFE65100), activeTrackColor = Color(0xFFE65100)))
                        Text("Evita que la señal supere este nivel para prevenir saturación digital.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                }
                DspEffect.BITCRUSHER -> {
                    ConfigCard(title = "Configuración: Bitcrusher", color = Color(0xFF5D4037)) {
                        Text("Mix: ${(bitcrusherMix * 100).toInt()}%")
                        Slider(value = bitcrusherMix, onValueChange = onBitcrusherMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF5D4037), activeTrackColor = Color(0xFF5D4037)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Reducción de Bits: ${(bitcrusherDepth * 100).toInt()}%")
                        Slider(value = bitcrusherDepth, onValueChange = onBitcrusherDepthChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF5D4037), activeTrackColor = Color(0xFF5D4037)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Downsampling: ${(bitcrusherRate * 100).toInt()}%")
                        Slider(value = bitcrusherRate, onValueChange = onBitcrusherRateChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF5D4037), activeTrackColor = Color(0xFF5D4037)))
                    }
                }
                DspEffect.PHASER -> {
                    ConfigCard(title = "Configuración: Phaser", color = Color(0xFF673AB7)) {
                        Text("Mix: ${(phaserMix * 100).toInt()}%")
                        Slider(value = phaserMix, onValueChange = onPhaserMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF673AB7), activeTrackColor = Color(0xFF673AB7)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Velocidad: ${String.format("%.1f", phaserRate)} Hz")
                        Slider(value = phaserRate, onValueChange = onPhaserRateChange, valueRange = 0.1f..10f, colors = SliderDefaults.colors(thumbColor = Color(0xFF673AB7), activeTrackColor = Color(0xFF673AB7)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profundidad: ${(phaserDepth * 100).toInt()}%")
                        Slider(value = phaserDepth, onValueChange = onPhaserDepthChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF673AB7), activeTrackColor = Color(0xFF673AB7)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Feedback: ${(phaserFeedback * 100).toInt()}%")
                        Slider(value = phaserFeedback, onValueChange = onPhaserFeedbackChange, valueRange = 0f..0.9f, colors = SliderDefaults.colors(thumbColor = Color(0xFF673AB7), activeTrackColor = Color(0xFF673AB7)))
                    }
                }
                DspEffect.NOISE_GATE -> {
                    ConfigCard(title = "Configuración: Noise Gate", color = Color(0xFF388E3C)) {
                        Text("Umbral (Threshold): ${String.format("%.3f", noiseGateThreshold)}")
                        Slider(
                            value = noiseGateThreshold, 
                            onValueChange = onNoiseGateThresholdChange, 
                            valueRange = 0f..0.5f, // Usually low range is needed
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF388E3C), activeTrackColor = Color(0xFF388E3C))
                        )
                        Text("Silencia el audio cuando el volumen está por debajo del umbral.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                    }
                }
                DspEffect.FLANGER -> {
                     ConfigCard(title = "Configuración: Flanger", color = Color(0xFFFBC02D)) {
                        Text("Mix: ${(flangerMix * 100).toInt()}%")
                        Slider(value = flangerMix, onValueChange = onFlangerMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFBC02D), activeTrackColor = Color(0xFFFBC02D)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Velocidad: ${String.format("%.1f", flangerRate)} Hz")
                        Slider(value = flangerRate, onValueChange = onFlangerRateChange, valueRange = 0.1f..2f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFBC02D), activeTrackColor = Color(0xFFFBC02D)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profundidad: ${String.format("%.1f", flangerDepth)} ms")
                        Slider(value = flangerDepth, onValueChange = onFlangerDepthChange, valueRange = 0.1f..5f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFBC02D), activeTrackColor = Color(0xFFFBC02D)))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Feedback: ${(flangerFeedback * 100).toInt()}%")
                        Slider(value = flangerFeedback, onValueChange = onFlangerFeedbackChange, valueRange = 0f..0.9f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFBC02D), activeTrackColor = Color(0xFFFBC02D)))
                    }
                }
                DspEffect.DISTORTION -> {
                    ConfigCard(title = "Configuración: Distorsión", color = Color(0xFFD32F2F)) {
                        Text("Drive: ${(distortion * 100).toInt()}%")
                        Slider(value = distortion, onValueChange = onDistortionChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFD32F2F), activeTrackColor = Color(0xFFD32F2F)))
                    }
                }
                DspEffect.EQ -> {
                    ConfigCard(title = "Configuración: Ecualizador", color = Color(0xFF1976D2)) {
                        EQ(modifier = Modifier.height(300.dp), eqBands = eqBands, bandLabels = bandLabels)
                    }
                }
                DspEffect.DELAY -> {
                    ConfigCard(title = "Configuración: Delay", color = Color(0xFF7B1FA2)) {
                        Text("Tiempo: ${String.format("%.2f", delayTime)} s")
                        Slider(value = delayTime, onValueChange = onDelayTimeChange, valueRange = 0.05f..2.0f, colors = SliderDefaults.colors(thumbColor = Color(0xFF7B1FA2), activeTrackColor = Color(0xFF7B1FA2)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Feedback: ${(delayFeedback * 100).toInt()}%")
                        Slider(value = delayFeedback, onValueChange = onDelayFeedbackChange, valueRange = 0f..0.9f, colors = SliderDefaults.colors(thumbColor = Color(0xFF7B1FA2), activeTrackColor = Color(0xFF7B1FA2)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mix: ${(delayMix * 100).toInt()}%")
                        Slider(value = delayMix, onValueChange = onDelayMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF7B1FA2), activeTrackColor = Color(0xFF7B1FA2)))
                    }
                }
                DspEffect.REVERB -> {
                    ConfigCard(title = "Configuración: Reverb", color = Color(0xFF455A64)) {
                        Text("Mix (Wet/Dry): ${(reverbMix * 100).toInt()}%")
                        Slider(value = reverbMix, onValueChange = onReverbMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF455A64), activeTrackColor = Color(0xFF455A64)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tamaño (Size): ${(reverbSize * 100).toInt()}%")
                        Slider(value = reverbSize, onValueChange = onReverbSizeChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF455A64), activeTrackColor = Color(0xFF455A64)))
                    }
                }
                DspEffect.COMPRESSOR -> {
                    ConfigCard(title = "Configuración: Compresor", color = Color(0xFFFFA000)) {
                        Text("Umbral (Threshold): ${String.format("%.2f", compThreshold)}")
                        Slider(value = compThreshold, onValueChange = onCompThresholdChange, valueRange = 0.001f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFA000), activeTrackColor = Color(0xFFFFA000)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ratio: ${String.format("%.1f", compRatio)}:1")
                        Slider(value = compRatio, onValueChange = onCompRatioChange, valueRange = 1f..20f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFA000), activeTrackColor = Color(0xFFFFA000)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ganancia (Makeup): ${String.format("%.1f", compMakeup)}x")
                        Slider(value = compMakeup, onValueChange = onCompMakeupChange, valueRange = 1f..4f, colors = SliderDefaults.colors(thumbColor = Color(0xFFFFA000), activeTrackColor = Color(0xFFFFA000)))
                    }
                }
                DspEffect.TREMOLO -> {
                    ConfigCard(title = "Configuración: Tremolo", color = Color(0xFF00796B)) {
                        Text("Profundidad: ${(tremDepth * 100).toInt()}%")
                        Slider(value = tremDepth, onValueChange = onTremDepthChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFF00796B), activeTrackColor = Color(0xFF00796B)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Velocidad: ${String.format("%.1f", tremRate)} Hz")
                        Slider(value = tremRate, onValueChange = onTremRateChange, valueRange = 0.1f..20f, colors = SliderDefaults.colors(thumbColor = Color(0xFF00796B), activeTrackColor = Color(0xFF00796B)))
                    }
                }
                DspEffect.CHORUS -> {
                    ConfigCard(title = "Configuración: Chorus", color = Color(0xFFC2185B)) {
                        Text("Mix: ${(chorusMix * 100).toInt()}%")
                        Slider(value = chorusMix, onValueChange = onChorusMixChange, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color(0xFFC2185B), activeTrackColor = Color(0xFFC2185B)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Velocidad: ${String.format("%.1f", chorusRate)} Hz")
                        Slider(value = chorusRate, onValueChange = onChorusRateChange, valueRange = 0.1f..5f, colors = SliderDefaults.colors(thumbColor = Color(0xFFC2185B), activeTrackColor = Color(0xFFC2185B)))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Profundidad: ${String.format("%.1f", chorusDepth)} ms")
                        Slider(value = chorusDepth, onValueChange = onChorusDepthChange, valueRange = 1f..10f, colors = SliderDefaults.colors(thumbColor = Color(0xFFC2185B), activeTrackColor = Color(0xFFC2185B)))
                    }
                }
                else -> {}
            }
        }
    }
}