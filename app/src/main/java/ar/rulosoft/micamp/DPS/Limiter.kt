package ar.rulosoft.micamp.DPS

import kotlin.math.abs

class Limiter(private val sampleRate: Int) {
    // A simple look-ahead limiter or just a hard limiter with soft knee?
    // For low latency realtime, look-ahead adds latency. 
    // Let's implement a simple peak limiter with fast attack and release.
    
    private var envelope = 0f
    private val attackTime = 0.001f // 1ms
    private val releaseTime = 0.05f // 50ms
    
    private val attackCoef = Math.exp(-1.0 / (sampleRate * attackTime)).toFloat()
    private val releaseCoef = Math.exp(-1.0 / (sampleRate * releaseTime)).toFloat()
    
    fun process(input: Float, threshold: Float): Float {
        // Threshold: 0.0 to 1.0 (absolute peak)
        // Usually limiters are set to slightly below 0dB (e.g. 0.95)
        
        val absInput = abs(input)
        
        if (absInput > envelope) {
            envelope = attackCoef * envelope + (1 - attackCoef) * absInput
        } else {
            envelope = releaseCoef * envelope + (1 - releaseCoef) * absInput
        }
        
        if (envelope > threshold) {
            val gain = threshold / envelope
            return input * gain
        }
        
        return input
    }
}
