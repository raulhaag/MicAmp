package ar.rulosoft.micamp.DPS

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

class AutoWah(private val sampleRate: Int) {
    // A simplified state variable filter (bandpass) modulated by an envelope follower
    
    private var envelope = 0f
    private val attackTime = 0.01f
    private val releaseTime = 0.05f
    private val attackCoef = exp(-1.0 / (sampleRate * attackTime)).toFloat()
    private val releaseCoef = exp(-1.0 / (sampleRate * releaseTime)).toFloat()
    
    // Filter state
    private var low = 0f
    private var band = 0f
    
    fun process(input: Float, depth: Float, rate: Float, mix: Float, resonance: Float = 0.5f): Float {
        // rate here is actually used as "sensitivity" or "frequency range" modifier in some auto-wahs, 
        // but often AutoWah is purely envelope driven. 
        // Let's treat 'rate' as LFO rate if we wanted LFO Wah, but for "Auto-Wah" (Envelope Filter),
        // usually we want Sensitivity (how much volume triggers it) and Range (Frequency limits).
        // To keep params consistent with generic controls:
        // Depth -> Range of frequency sweep
        // Rate -> Sensitivity (how easily the envelope opens) - Reusing the label
        
        // Envelope follower
        val inputAbs = abs(input)
        if (inputAbs > envelope) {
            envelope = attackCoef * envelope + (1 - attackCoef) * inputAbs
        } else {
            envelope = releaseCoef * envelope + (1 - releaseCoef) * inputAbs
        }
        
        // Calculate cutoff frequency based on envelope
        // Base freq ~500Hz, moving up to ~3000Hz based on depth * envelope * sensitivity
        val minFreq = 200f
        val maxFreq = 3000f
        val sensitivity = rate * 10f // Scale rate 0-1 to 0-10 gain
        
        var cutoff = minFreq + (maxFreq - minFreq) * depth * min(1f, envelope * sensitivity)
        cutoff = cutoff.coerceIn(minFreq, maxFreq)
        
        // State Variable Filter (Chamberlin version or similar) implementation
        // f = 2 * sin(pi * cutoff / samplerate)
        // q = 1 / Q (damping) -> resonance usually 0..1 -> Q
        
        val f = (2.0 * sin(Math.PI * cutoff / sampleRate)).toFloat()
        val q = 0.1f + (1f - resonance) * 0.5f // Damping
        
        low += f * band
        val high = input - low - q * band
        band += f * high
        
        // Bandpass output is usually the "wah" sound
        val wet = band 
        
        return input * (1 - mix) + wet * mix * 3.0f // Boost wet slightly as BP can be quiet
    }
}
