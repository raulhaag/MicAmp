package ar.rulosoft.micamp.DPS

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

class Compressor(private val sampleRate: Int) {
    private var envelope = 0f

    fun process(input: Float, threshold: Float, ratio: Float, makeupGain: Float): Float {
        // Simple Envelope Follower
        val absInput = abs(input)
        val attackCoeff = exp(-1f / (0.01f * sampleRate)) // 10ms attack
        val releaseCoeff = exp(-1f / (0.1f * sampleRate)) // 100ms release

        envelope = if (absInput > envelope) {
            attackCoeff * envelope + (1 - attackCoeff) * absInput
        } else {
            releaseCoeff * envelope + (1 - releaseCoeff) * absInput
        }

        // Convert to dB
        val envDb = if (envelope > 1e-6) 20 * kotlin.math.log10(envelope) else -120f
        val threshDb = 20 * kotlin.math.log10(threshold)

        // Gain Reduction
        var gainReductionDb = 0f
        if (envDb > threshDb) {
            gainReductionDb = (threshDb - envDb) * (1 - 1/ratio)
        }

        // Convert back to linear gain
        // Now this will work because 10.0 is a Double and pow is an extension function on Double
        val gain = 10.0.pow((gainReductionDb / 20.0).toDouble()).toFloat()

        return input * gain * makeupGain
    }
}
