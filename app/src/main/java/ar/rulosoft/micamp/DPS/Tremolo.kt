package ar.rulosoft.micamp.DPS

import kotlin.math.PI
import kotlin.math.sin

class Tremolo(private val sampleRate: Int) {
    private var phase = 0.0
    
    fun process(input: Float, depth: Float, rate: Float): Float {
        // LFO (Low Frequency Oscillator)
        // Simple sine wave 0..1 modulation
        // depth: 0..1 (how much modulation)
        // rate: Hz (speed)
        
        val phaseIncrement = (2 * PI * rate) / sampleRate
        phase += phaseIncrement
        if (phase > 2 * PI) phase -= 2 * PI
        
        val lfo = (sin(phase) + 1.0) / 2.0 // 0 to 1
        
        val modulation = 1.0 - (depth * lfo) // 1 down to (1-depth)
        
        return (input * modulation).toFloat()
    }
}
