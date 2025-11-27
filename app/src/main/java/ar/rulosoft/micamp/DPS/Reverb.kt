package ar.rulosoft.micamp.DPS

class Reverb(sampleRate: Int) {
    // Schroeder Reverb Implementation
    // 4 Parallel Comb Filters
    // 2 Series All-Pass Filters
    
    private val combs = arrayOf(
        Comb(calculateDelay(1557, sampleRate)),
        Comb(calculateDelay(1617, sampleRate)),
        Comb(calculateDelay(1491, sampleRate)),
        Comb(calculateDelay(1422, sampleRate))
    )
    
    private val allPass1 = AllPass(calculateDelay(225, sampleRate))
    private val allPass2 = AllPass(calculateDelay(341, sampleRate))
    
    private fun calculateDelay(samples44k: Int, currentRate: Int): Int {
        return (samples44k * currentRate / 44100)
    }

    fun process(input: Float, mix: Float, size: Float): Float {
        val feedback = 0.7f + (0.28f * size) // 0.7 to 0.98
        
        var out = 0f
        for (comb in combs) {
            out += comb.process(input, feedback)
        }
        
        out = allPass1.process(out)
        out = allPass2.process(out)
        
        return (input * (1f - mix)) + (out * mix * 0.5f) // 0.5f to attenuate potential gain
    }
    
    private class Comb(val size: Int) {
        private val buffer = FloatArray(size)
        private var index = 0
        
        fun process(input: Float, feedback: Float): Float {
            val output = buffer[index]
            buffer[index] = input + (output * feedback)
            index = (index + 1) % size
            return output
        }
    }
    
    private class AllPass(val size: Int) {
        private val buffer = FloatArray(size)
        private var index = 0
        
        fun process(input: Float): Float {
            val bufOut = buffer[index]
            val output = -input + bufOut
            buffer[index] = input + (bufOut * 0.5f)
            index = (index + 1) % size
            return output
        }
    }
}
