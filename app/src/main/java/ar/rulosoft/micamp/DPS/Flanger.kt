package ar.rulosoft.micamp.DPS

import kotlin.math.PI
import kotlin.math.sin

class Flanger(private val sampleRate: Int) {
    // A flanger is a modulated delay with very short delay times (1-10ms) and feedback
    private val maxDelayMs = 15 // 10ms is typical max for flanger
    private val maxDelaySamples = (maxDelayMs * sampleRate / 1000)
    private val buffer = FloatArray(maxDelaySamples + 100)
    private var writeIndex = 0
    private var phase = 0.0
    
    fun process(input: Float, rate: Float, depth: Float, mix: Float, feedback: Float): Float {
        // rate: LFO speed in Hz (e.g., 0.1 to 1 Hz)
        // depth: modulation depth in ms (e.g., 1 to 5 ms)
        // mix: dry/wet mix (0 to 1)
        // feedback: regeneration (0 to <1)
        
        val depthSamples = (depth * sampleRate / 1000).coerceIn(0f, maxDelaySamples.toFloat() / 2f)
        val baseDelaySamples = (3 * sampleRate / 1000).toFloat() // 3ms base delay
        
        // LFO
        val phaseIncrement = (2 * PI * rate) / sampleRate
        phase += phaseIncrement
        if (phase > 2 * PI) phase -= 2 * PI
        val lfo = sin(phase)
        
        // Modulate delay time
        val currentDelay = baseDelaySamples + (lfo * depthSamples)
        
        var readIndex = writeIndex - currentDelay
        while (readIndex < 0) readIndex += maxDelaySamples
        while (readIndex >= maxDelaySamples) readIndex -= maxDelaySamples
        
        // Linear Interpolation
        val indexInt = readIndex.toInt()
        val frac = readIndex - indexInt
        val nextIndex = (indexInt + 1) % maxDelaySamples
        
        val delayedSample = buffer[indexInt] * (1 - frac) + buffer[nextIndex] * frac
        
        // Write to buffer with feedback
        // Flanger feedback is key to the "jet" sound
        val nextWriteSample = input + (delayedSample.toFloat() * feedback)
        buffer[writeIndex] = nextWriteSample
        writeIndex = (writeIndex + 1) % maxDelaySamples
        
        return (input * (1 - mix)) + (delayedSample.toFloat() * mix)
    }
}
