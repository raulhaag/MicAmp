package ar.rulosoft.micamp.ui.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val updatedOnValueChange by rememberUpdatedState(onValueChange)
    val updatedValue by rememberUpdatedState(value)
    var height by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .width(30.dp)
            .onSizeChanged { height = it.height.coerceAtLeast(1) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    val rangeSize = range.endInclusive - range.start
                    // delta is in pixels. Up is negative.
                    // We want value to increase when dragging up.
                    val valueChange = (-delta / height.toFloat()) * rangeSize
                    val newValue = (updatedValue + valueChange).coerceIn(range)
                    updatedOnValueChange(newValue)
                }
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Track
            drawRoundRect(
                color = Color.LightGray,
                size = Size(4.dp.toPx(), size.height),
                topLeft = Offset((size.width - 4.dp.toPx()) / 2, 0f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Draw Fill
            val rangeSize = range.endInclusive - range.start
            val safeRangeSize = if (rangeSize == 0f) 1f else rangeSize
            
            // Map value to Y position.
            // Max value is at Y=0 (top). Min value is at Y=height (bottom).
            val currentY = size.height * (range.endInclusive - value) / safeRangeSize

            // Draw Thumb
            drawCircle(
                color = Color(0xFF6200EE),
                radius = 8.dp.toPx(),
                center = Offset(size.width / 2, currentY)
            )
        }
    }
}
