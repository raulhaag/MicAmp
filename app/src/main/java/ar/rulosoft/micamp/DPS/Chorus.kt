package ar.rulosoft.micamp.DPS

import kotlin.math.PI
import kotlin.math.sin

class Chorus(private val sampleRate: Int) {
    // A simple chorus is essentially a modulated delay
    private val maxDelayMs = 30 // 20-30ms is typical for chorus
    private val maxDelaySamples = (maxDelayMs * sampleRate / 1000)
    private val buffer = FloatArray(maxDelaySamples + 100) // +100 safety
    private var writeIndex = 0
    private var phase = 0.0
    
    fun process(input: Float, rate: Float, depth: Float, mix: Float): Float {
        // rate: LFO speed in Hz (e.g., 0.5 to 5 Hz)
        // depth: modulation depth in ms (e.g., 1 to 10 ms)
        // mix: dry/wet mix (0 to 1)
        
        val depthSamples = (depth * sampleRate / 1000).coerceIn(0f, maxDelaySamples.toFloat() / 2f)
        val baseDelaySamples = (15 * sampleRate / 1000).toFloat() // 15ms base delay
        
        // LFO
        val phaseIncrement = (2 * PI * rate) / sampleRate
        phase += phaseIncrement
        if (phase > 2 * PI) phase -= 2 * PI
        val lfo = sin(phase)
        
        val currentDelay = baseDelaySamples + (lfo * depthSamples)
        
        var readIndex = writeIndex - currentDelay
        while (readIndex < 0) readIndex += maxDelaySamples
        while (readIndex >= maxDelaySamples) readIndex -= maxDelaySamples
        
        // Linear Interpolation
        val indexInt = readIndex.toInt()
        val frac = readIndex - indexInt
        val nextIndex = (indexInt + 1) % maxDelaySamples
        
        val delayedSample = buffer[indexInt] * (1 - frac) + buffer[nextIndex] * frac
        
        buffer[writeIndex] = input
        writeIndex = (writeIndex + 1) % maxDelaySamples
        
        return (input * (1 - mix)) + (delayedSample.toFloat() * mix)
    }
}
