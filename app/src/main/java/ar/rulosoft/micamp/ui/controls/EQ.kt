package ar.rulosoft.micamp.ui.controls;

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.Composable;

@Composable
fun EQ(modifier: Modifier = Modifier, eqBands: MutableList<Float>, bandLabels: List<String>){
    // Compact Vertical EQ
    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        eqBands.forEachIndexed { index, gain ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                VerticalSlider(
                    value = gain,
                    onValueChange = { eqBands[index] = it },
                    range = -12f..12f,
                    modifier = Modifier.weight(1f)
                )
                Text(bandLabels[index], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                Text("${gain.toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
