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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.rulosoft.micamp.ui.SvgIcons

// Enum for identifying which configuration we are viewing
enum class DspEffect {
    NONE, DISTORTION, EQ, DELAY, REVERB, COMPRESSOR, TREMOLO, CHORUS, NOISE_GATE, FLANGER, PHASER, BITCRUSHER, LIMITER, AUTO_WAH
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
    
    isNoiseGateEnabled: Boolean,
    onNoiseGateEnabledChange: (Boolean) -> Unit,

    isFlangerEnabled: Boolean,
    onFlangerEnabledChange: (Boolean) -> Unit,
    
    isPhaserEnabled: Boolean,
    onPhaserEnabledChange: (Boolean) -> Unit,

    isBitcrusherEnabled: Boolean,
    onBitcrusherEnabledChange: (Boolean) -> Unit,

    isLimiterEnabled: Boolean,
    onLimiterEnabledChange: (Boolean) -> Unit,

    isAutoWahEnabled: Boolean,
    onAutoWahEnabledChange: (Boolean) -> Unit,
    
    onOpenSettings: (DspEffect) -> Unit,
    onPresetsClick: () -> Unit
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Efectos DSP",
                style = MaterialTheme.typography.titleSmall
            )
            OutlinedButton(
                onClick = onPresetsClick,
                modifier = Modifier.height(35.dp)
            ) {
                Text("Presets")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- DSP BUTTONS (Scrollable) ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Row 0: Noise Gate & Auto Wah
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DspButton(
                        icon = SvgIcons.NoiseGate,
                        description = "Noise Gate",
                        isEnabled = isNoiseGateEnabled,
                        activeColor = Color(0xFF388E3C), // Green
                        onClick = { onNoiseGateEnabledChange(!isNoiseGateEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.NOISE_GATE) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DspButton(
                        icon = SvgIcons.AutoWah,
                        description = "Auto Wah",
                        isEnabled = isAutoWahEnabled,
                        activeColor = Color(0xFFFFD600), // Yellow
                        onClick = { onAutoWahEnabledChange(!isAutoWahEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.AUTO_WAH) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Row 1: Compressor & Limiter
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DspButton(
                        icon = SvgIcons.Compressor,
                        description = "Compresor",
                        isEnabled = isCompressorEnabled,
                        activeColor = Color(0xFFFFA000), // Amber
                        onClick = { onCompressorEnabledChange(!isCompressorEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.COMPRESSOR) },
                        modifier = Modifier.weight(1f)
                    )

                    DspButton(
                        icon = SvgIcons.Limiter,
                        description = "Limitador",
                        isEnabled = isLimiterEnabled,
                        activeColor = Color(0xFFE65100), // Dark Orange
                        onClick = { onLimiterEnabledChange(!isLimiterEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.LIMITER) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Row 2: Bitcrusher & Distortion
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DspButton(
                        icon = SvgIcons.Bitcrusher,
                        description = "Bitcrusher",
                        isEnabled = isBitcrusherEnabled,
                        activeColor = Color(0xFF5D4037), // Brown
                        onClick = { onBitcrusherEnabledChange(!isBitcrusherEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.BITCRUSHER) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    DspButton(
                        icon = SvgIcons.Distortion,
                        description = "Distorsión",
                        isEnabled = isDistortionEnabled,
                        activeColor = Color(0xFFD32F2F), // Red
                        onClick = { onDistortionEnabledChange(!isDistortionEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.DISTORTION) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Row 3: EQ & Tremolo
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     DspButton(
                        icon = SvgIcons.Equalizer,
                        description = "Ecualizador",
                        isEnabled = isEqEnabled,
                        activeColor = Color(0xFF1976D2), // Blue
                        onClick = { onEqEnabledChange(!isEqEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.EQ) },
                        modifier = Modifier.weight(1f)
                    )

                    DspButton(
                        icon = SvgIcons.Tremolo,
                        description = "Tremolo",
                        isEnabled = isTremoloEnabled,
                        activeColor = Color(0xFF00796B), // Teal
                        onClick = { onTremoloEnabledChange(!isTremoloEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.TREMOLO) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Row 4: Phaser & Flanger
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DspButton(
                        icon = SvgIcons.Phaser,
                        description = "Phaser",
                        isEnabled = isPhaserEnabled,
                        activeColor = Color(0xFF673AB7), // Deep Purple
                        onClick = { onPhaserEnabledChange(!isPhaserEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.PHASER) },
                        modifier = Modifier.weight(1f)
                    )

                    DspButton(
                        icon = SvgIcons.Flanger,
                        description = "Flanger",
                        isEnabled = isFlangerEnabled,
                        activeColor = Color(0xFFFBC02D), // Yellow
                        onClick = { onFlangerEnabledChange(!isFlangerEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.FLANGER) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Row 5: Chorus & Delay
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     DspButton(
                        icon = SvgIcons.Chorus,
                        description = "Chorus",
                        isEnabled = isChorusEnabled,
                        activeColor = Color(0xFFC2185B), // Pink
                        onClick = { onChorusEnabledChange(!isChorusEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.CHORUS) },
                        modifier = Modifier.weight(1f)
                    )

                    DspButton(
                        icon = SvgIcons.Delay,
                        description = "Delay",
                        isEnabled = isDelayEnabled,
                        activeColor = Color(0xFF7B1FA2), // Purple
                        onClick = { onDelayEnabledChange(!isDelayEnabled) },
                        onLongClick = { onOpenSettings(DspEffect.DELAY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Row 6: Reverb
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DspButton(
                        icon = SvgIcons.Reverb,
                        description = "Reverb",
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DspButton(
    icon: ImageVector,
    description: String,
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
            .height(60.dp) // Aumentamos ligeramente la altura para acomodar el texto
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
            Icon(
                imageVector = icon,
                contentDescription = null, // La descripción ahora es visual
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = contentColor,
                maxLines = 1
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
