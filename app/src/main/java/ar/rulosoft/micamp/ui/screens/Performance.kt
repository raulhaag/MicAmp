package ar.rulosoft.micamp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ar.rulosoft.micamp.ui.controls.EQ

// Enum for identifying which configuration we are viewing
enum class DspEffect {
    NONE, DISTORTION, EQ, DELAY, REVERB, COMPRESSOR, TREMOLO, CHORUS
}

@Composable
fun PerformanceScreen(
    modifier: Modifier = Modifier,
    visualizerData: FloatArray,
    
    isDistortionEnabled: Boolean,
    onDistortionEnabledChange: (Boolean) -> Unit,
    
    isEqEnabled: Boolean,
    onEqEnabledChange: (Boolean) -> Unit,
    
    isDelayEnabled: Boolean,
    onDelayEnabledChange: (Boolean) -> Unit,
    
    isReverbEnabled: Boolean,
    onReverbEnabledChange: (Boolean) -> Unit,
    
    isCompressorEnabled: Boolean,
    onCompressorEnabledChange: (Boolean) -> Unit,
    
    isTremoloEnabled: Boolean,
    onTremoloEnabledChange: (Boolean) -> Unit,
    
    isChorusEnabled: Boolean,
    onChorusEnabledChange: (Boolean) -> Unit,
    
    onOpenSettings: (DspEffect) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // --- VISUALIZER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(8.dp)
                .background(Color.Black, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val path = Path()
                path.moveTo(0f, centerY)
                if (visualizerData.isNotEmpty()) {
                    val stepX = width / (visualizerData.size - 1)
                    for (i in visualizerData.indices) {
                        val x = i * stepX
                        val y = centerY - (visualizerData[i] * height * 0.8f)
                        path.lineTo(x, y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.Green,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Efectos DSP",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- DSP BUTTONS ROWS ---
        
        // Row 1: Compressor & EQ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DspButton(
                text = "Compresor",
                isEnabled = isCompressorEnabled,
                activeColor = Color(0xFFFFA000), // Amber
                onClick = { onCompressorEnabledChange(!isCompressorEnabled) },
                onLongClick = { onOpenSettings(DspEffect.COMPRESSOR) },
                modifier = Modifier.weight(1f)
            )

            DspButton(
                text = "Ecualizador",
                isEnabled = isEqEnabled,
                activeColor = Color(0xFF1976D2), // Blue
                onClick = { onEqEnabledChange(!isEqEnabled) },
                onLongClick = { onOpenSettings(DspEffect.EQ) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Row 2: Distortion & Tremolo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DspButton(
                text = "Distorsión",
                isEnabled = isDistortionEnabled,
                activeColor = Color(0xFFD32F2F), // Red
                onClick = { onDistortionEnabledChange(!isDistortionEnabled) },
                onLongClick = { onOpenSettings(DspEffect.DISTORTION) },
                modifier = Modifier.weight(1f)
            )
            
            DspButton(
                text = "Tremolo",
                isEnabled = isTremoloEnabled,
                activeColor = Color(0xFF00796B), // Teal
                onClick = { onTremoloEnabledChange(!isTremoloEnabled) },
                onLongClick = { onOpenSettings(DspEffect.TREMOLO) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Row 3: Chorus & Delay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
             DspButton(
                text = "Chorus",
                isEnabled = isChorusEnabled,
                activeColor = Color(0xFFC2185B), // Pink
                onClick = { onChorusEnabledChange(!isChorusEnabled) },
                onLongClick = { onOpenSettings(DspEffect.CHORUS) },
                modifier = Modifier.weight(1f)
            )

            DspButton(
                text = "Delay",
                isEnabled = isDelayEnabled,
                activeColor = Color(0xFF7B1FA2), // Purple
                onClick = { onDelayEnabledChange(!isDelayEnabled) },
                onLongClick = { onOpenSettings(DspEffect.DELAY) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Row 4: Reverb
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DspButton(
                text = "Reverb",
                isEnabled = isReverbEnabled,
                activeColor = Color(0xFF455A64), // Blue Grey
                onClick = { onReverbEnabledChange(!isReverbEnabled) },
                onLongClick = { onOpenSettings(DspEffect.REVERB) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

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
    onChorusMixChange: (Float) -> Unit = {}
) {
    val title = when(effect) {
        DspEffect.DISTORTION -> "Distorsión"
        DspEffect.EQ -> "Ecualizador"
        DspEffect.DELAY -> "Delay"
        DspEffect.REVERB -> "Reverb"
        DspEffect.COMPRESSOR -> "Compresor"
        DspEffect.TREMOLO -> "Tremolo"
        DspEffect.CHORUS -> "Chorus"
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DspButton(
    text: String,
    isEnabled: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Definimos colores
    val backgroundColor = if (isEnabled) activeColor else Color.Transparent
    val contentColor = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (!isEnabled) activeColor else Color.Transparent

    Box(
        modifier = modifier
            .height(45.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ConfigCard(
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
