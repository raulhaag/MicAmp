package ar.rulosoft.micamp.DPS

import kotlin.math.round

class Bitcrusher(private val sampleRate: Int) {
    private var counter = 0f
    private var holdSample = 0f
    
    fun process(input: Float, depth: Float, rate: Float, mix: Float): Float {
        // Depth: Bit reduction amount (0.0 to 1.0)
        //   0.0 -> full 16 bit
        //   1.0 -> 1 bit (extreme)
        //   Internal mapping: 16 bits down to 1 bit
        
        // Rate: Sample rate reduction (0.0 to 1.0)
        //   0.0 -> full sample rate
        //   1.0 -> very low sample rate (e.g. / 50)
        
        // 1. Sample Rate Reduction (Downsampling)
        val step = 1f + rate * 49f // 1 to 50 samples hold
        counter += 1f
        if (counter >= step) {
            counter -= step
            holdSample = input
        }
        
        // 2. Bit Reduction (Quantization)
        // Map depth 0..1 to bits 16..1
        val bits = 16f - (depth * 15f)
        val levels = Math.pow(2.0, bits.toDouble()).toFloat()
        
        val quantized = round(holdSample * levels) / levels
        
        // Mix
        return input * (1 - mix) + quantized * mix
    }
}
