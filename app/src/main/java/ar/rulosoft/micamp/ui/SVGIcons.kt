package ar.rulosoft.micamp.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SvgIcons {
    val Record = ImageVector.Builder(name = "Record", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Red)) {
        moveTo(12f, 12f); moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f); curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close()
    }.build()
    val Stop = ImageVector.Builder(name = "Stop", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); close()
    }.build()
    
    val Compressor = ImageVector.Builder(name = "Compressor", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
         // Top Plate
        moveTo(4f, 2f); lineTo(20f, 2f); lineTo(20f, 6f); lineTo(4f, 6f); close()
        // Bottom Plate
        moveTo(4f, 18f); lineTo(20f, 18f); lineTo(20f, 22f); lineTo(4f, 22f); close()
        // Block
        moveTo(8f, 8f); lineTo(16f, 8f); lineTo(16f, 16f); lineTo(8f, 16f); close()
    }.build()

    val Equalizer = ImageVector.Builder(name = "Equalizer", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        // Track 1
        moveTo(5f, 20f); lineTo(7f, 20f); lineTo(7f, 4f); lineTo(5f, 4f); close()
        // Knob 1
        moveTo(4f, 14f); lineTo(8f, 14f); lineTo(8f, 16f); lineTo(4f, 16f); close()
        
        // Track 2
        moveTo(11f, 20f); lineTo(13f, 20f); lineTo(13f, 4f); lineTo(11f, 4f); close()
        // Knob 2
        moveTo(10f, 8f); lineTo(14f, 8f); lineTo(14f, 10f); lineTo(10f, 10f); close()
        
        // Track 3
        moveTo(17f, 20f); lineTo(19f, 20f); lineTo(19f, 4f); lineTo(17f, 4f); close()
        // Knob 3
        moveTo(16f, 12f); lineTo(20f, 12f); lineTo(20f, 14f); lineTo(16f, 14f); close()
    }.build()

    val Distortion = ImageVector.Builder(name = "Distortion", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(11f, 2f); lineTo(11f, 9f); lineTo(6f, 9f); lineTo(13f, 22f); lineTo(13f, 13f); lineTo(18f, 13f); close()
    }.build()

    val Tremolo = ImageVector.Builder(name = "Tremolo", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(2f, 12f)
        curveTo(7f, 2f, 12f, 22f, 22f, 12f)
        lineTo(22f, 15f)
        curveTo(12f, 25f, 7f, 5f, 2f, 15f)
        close()
    }.build()
    
    val Chorus = ImageVector.Builder(name = "Chorus", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(2f, 9f); curveTo(7f, -1f, 12f, 19f, 22f, 9f); lineTo(22f, 10f); curveTo(12f, 20f, 7f, 0f, 2f, 10f); close()
        moveTo(2f, 14f); curveTo(7f, 4f, 12f, 24f, 22f, 14f); lineTo(22f, 15f); curveTo(12f, 25f, 7f, 5f, 2f, 15f); close()
    }.build()

    val Delay = ImageVector.Builder(name = "Delay", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
         moveTo(3f, 20f); lineTo(5f, 20f); lineTo(5f, 6f); lineTo(3f, 6f); close()
         moveTo(9f, 20f); lineTo(11f, 20f); lineTo(11f, 10f); lineTo(9f, 10f); close()
         moveTo(15f, 20f); lineTo(17f, 20f); lineTo(17f, 14f); lineTo(15f, 14f); close()
         moveTo(21f, 20f); lineTo(23f, 20f); lineTo(23f, 17f); lineTo(21f, 17f); close()
    }.build()

    val Reverb = ImageVector.Builder(name = "Reverb", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(4f, 20f); lineTo(4f, 4f); lineTo(6f, 4f); lineTo(6f, 20f); close()
        moveTo(10f, 18f); curveTo(14f, 16f, 14f, 8f, 10f, 6f); lineTo(11f, 5f); curveTo(16f, 8f, 16f, 16f, 11f, 19f); close()
        moveTo(16f, 22f); curveTo(22f, 18f, 22f, 6f, 16f, 2f); lineTo(17f, 1f); curveTo(24f, 6f, 24f, 18f, 17f, 23f); close()
    }.build()

    val NoiseGate = ImageVector.Builder(name = "NoiseGate", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(4f, 4f); lineTo(6f, 4f); lineTo(6f, 20f); lineTo(4f, 20f); close() 
        moveTo(18f, 4f); lineTo(20f, 4f); lineTo(20f, 20f); lineTo(18f, 20f); close()
        moveTo(6f, 11f); lineTo(18f, 11f); lineTo(18f, 13f); lineTo(6f, 13f); close()
    }.build()

    val Flanger = ImageVector.Builder(name = "Flanger", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 2f); curveTo(2f, 12f, 12f, 22f, 12f, 22f); curveTo(22f, 12f, 12f, 2f, 12f, 2f)
        moveTo(12f, 6f); curveTo(8f, 12f, 12f, 18f, 12f, 18f); curveTo(16f, 12f, 12f, 6f, 12f, 6f)
    }.build()

    val Phaser = ImageVector.Builder(name = "Phaser", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f); curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close()
        moveTo(12f, 18f); curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f); curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f); curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f); curveTo(18f, 15.31f, 15.31f, 18f, 12f, 18f); close()
    }.build()

    val Bitcrusher = ImageVector.Builder(name = "Bitcrusher", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(2f, 2f); lineTo(6f, 2f); lineTo(6f, 6f); lineTo(10f, 6f); lineTo(10f, 10f); lineTo(14f, 10f); lineTo(14f, 14f); lineTo(18f, 14f); lineTo(18f, 18f); lineTo(22f, 18f); lineTo(22f, 22f); lineTo(18f, 22f); lineTo(18f, 18f); lineTo(14f, 18f); lineTo(14f, 14f); lineTo(10f, 14f); lineTo(10f, 10f); lineTo(6f, 10f); lineTo(6f, 6f); lineTo(2f, 6f); close()
    }.build()

    val Limiter = ImageVector.Builder(name = "Limiter", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
        moveTo(4f, 4f); lineTo(20f, 4f); lineTo(20f, 6f); lineTo(4f, 6f); close()
        moveTo(6f, 8f); lineTo(10f, 8f); lineTo(10f, 20f); lineTo(6f, 20f); close()
        moveTo(14f, 12f); lineTo(18f, 12f); lineTo(18f, 20f); lineTo(14f, 20f); close()
    }.build()

    val AutoWah = ImageVector.Builder(name = "AutoWah", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).path(fill = SolidColor(Color.Black)) {
         moveTo(12f, 3f); curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f); curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f); curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f); curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
         moveTo(12f, 19f); curveTo(8.13f, 19f, 5f, 15.87f, 5f, 12f); curveTo(5f, 8.13f, 8.13f, 5f, 12f, 5f); curveTo(15.87f, 5f, 19f, 8.13f, 19f, 12f); curveTo(19f, 15.87f, 15.87f, 19f, 12f, 19f)
         moveTo(12f, 8f); curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f); curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f); curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f); curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
    }.build()
}
