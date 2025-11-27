package ar.rulosoft.micamp.ui.controls

import android.media.AudioDeviceInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp

@Composable
fun DeviceSelector(
    modifier: Modifier = Modifier,
    inputDevices: List<AudioDeviceInfo>,
    outputDevices: List<AudioDeviceInfo>,
    selectedInputDevice: AudioDeviceInfo?,
    selectedOutputDevice: AudioDeviceInfo?,
    onInputSelected: (AudioDeviceInfo) -> Unit,
    onOutputSelected: (AudioDeviceInfo) -> Unit
) {
    // Coordinates for drawing the cable
    var inputPoint by remember { mutableStateOf(Offset.Unspecified) }
    var outputPoint by remember { mutableStateOf(Offset.Unspecified) }
    var canvasPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                canvasPosition = coordinates.positionInRoot()
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp)
            ) {
                Text(
                    "Entradas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                inputDevices.forEach { device ->
                    val isSelected = device.id == selectedInputDevice?.id
                    DeviceItem(
                        name = "${device.productName}\n(${typeToString(device.type)})",
                        isSelected = isSelected,
                        isInput = true,
                        onClick = { onInputSelected(device) },
                        onPositioned = { offset -> if (isSelected) inputPoint = offset }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 8.dp)
            ) {
                Text(
                    "Salidas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                outputDevices.forEach { device ->
                    val isSelected = device.id == selectedOutputDevice?.id
                    DeviceItem(
                        name = "${device.productName}\n(${typeToString(device.type)})",
                        isSelected = isSelected,
                        isInput = false,
                        onClick = { onOutputSelected(device) },
                        onPositioned = { offset -> if (isSelected) outputPoint = offset }
                    )
                }
            }
        }

        if (inputPoint != Offset.Unspecified && outputPoint != Offset.Unspecified) {
            val lineColor = Color(201, 83, 46)
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInputPassThrough()) {
                val start = inputPoint - canvasPosition
                val end = outputPoint - canvasPosition
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(start.x + 100f, start.y, end.x - 100f, end.y, end.x, end.y)
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
}

@Composable
fun DeviceItem(
    name: String,
    isSelected: Boolean,
    isInput: Boolean,
    onClick: () -> Unit,
    onPositioned: (Offset) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        ) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .onGloballyPositioned { coordinates ->
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
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (isInput) Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
            )
        }
    }
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

fun Modifier.pointerInputPassThrough(): Modifier = this
