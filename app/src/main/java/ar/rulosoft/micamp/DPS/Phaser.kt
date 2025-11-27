package ar.rulosoft.micamp.DPS

import kotlin.math.PI
import kotlin.math.sin

class Phaser(private val sampleRate: Int) {
    private val allpassStages = Array(6) { AllPass() }
    private var phase = 0.0
    private var lastOutput = 0f

    fun process(input: Float, rate: Float, depth: Float, feedback: Float, mix: Float): Float {
        // rate: 0.1 - 10 Hz
        // depth: 0 - 1 (sweeping range)
        // feedback: 0 - 0.9
        
        phase += (2 * PI * rate) / sampleRate
        if (phase > 2 * PI) phase -= 2 * PI
        
        val lfo = (sin(phase).toFloat() + 1f) / 2f // 0..1
        
        // Map LFO to allpass coefficient 'a'
        // -0.9 to 0.9 covers most of the frequency range
        // depth controls how wide the sweep is
        val minVal = -0.5f
        val maxVal = 0.5f
        
        // a = base + lfo * width
        // let's center it around 0
        val a = (lfo - 0.5f) * 1.8f * depth // Range roughly -0.9 to 0.9 at max depth
        
        var inSignal = input + lastOutput * feedback
        // Soft clip feedback
        if (inSignal > 2.0f) inSignal = 2.0f
        if (inSignal < -2.0f) inSignal = -2.0f
        
        var processed = inSignal
        for (stage in allpassStages) {
            processed = stage.process(processed, a)
        }
        
        lastOutput = processed
        
        return input * (1 - mix) + processed * mix
    }

    class AllPass {
        var x1 = 0f
        var y1 = 0f
        
        fun process(x: Float, a: Float): Float {
            // y[n] = a * x[n] + x[n-1] - a * y[n-1]
            val y = a * x + x1 - a * y1
            x1 = x
            y1 = y
            return y
        }
    }
}
