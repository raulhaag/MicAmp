package ar.rulosoft.micamp.DPS

class NoiseGate(private val sampleRate: Int) {
    private var envelope = 0f
    // Time constants for attack and release (in seconds)
    private val attackTime = 0.01f // 10ms
    private val releaseTime = 0.1f // 100ms
    
    private val attackCoef = Math.exp(-1.0 / (sampleRate * attackTime)).toFloat()
    private val releaseCoef = Math.exp(-1.0 / (sampleRate * releaseTime)).toFloat()

    fun process(input: Float, threshold: Float): Float {
        // threshold: 0.0 to 1.0 (amplitude)
        
        val inputAbs = Math.abs(input)
        
        // Envelope follower
        if (inputAbs > envelope) {
            envelope = attackCoef * envelope + (1 - attackCoef) * inputAbs
        } else {
            envelope = releaseCoef * envelope + (1 - releaseCoef) * inputAbs
        }
        
        // Gain reduction
        // Hard knee for simplicity: 0 if below threshold, 1 if above
        // But we smooth it via the envelope so it's not choppy
        
        val gain = if (envelope >= threshold) 1.0f else 0.0f
        
        // Simple smoothing for the gain transition could be added here if needed, 
        // but the envelope follower already provides some hysteresis. 
        // However, strict hard gating can click. 
        // Let's do a simple "soft" gate using a small window or just return hard gated value for now
        // as it's requested as "Noise Gate".
        
        // For a more musical gate, we can slew the gain.
        // But for "removes background noise", hard cutoff below threshold is the core idea.
        
        // To prevent clicking, we can apply a small slew to the gain itself (attack/release on the gain)
        // But let's stick to a simple implementation:
        
        return if (envelope < threshold) 0f else input
    }
}
