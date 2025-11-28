package ar.rulosoft.micamp.data

enum class EffectType {
    NOISE_GATE,
    COMPRESSOR,
    AUTO_WAH,
    BITCRUSHER,
    EQ,
    DISTORTION,
    PHASER,
    FLANGER,
    TREMOLO,
    CHORUS,
    DELAY,
    REVERB,
    LIMITER;
    
    fun getLabel(): String {
        return when(this) {
            NOISE_GATE -> "Noise Gate"
            COMPRESSOR -> "Compresor"
            AUTO_WAH -> "Auto Wah"
            BITCRUSHER -> "Bitcrusher"
            EQ -> "Ecualizador"
            DISTORTION -> "Distorsión"
            PHASER -> "Phaser"
            FLANGER -> "Flanger"
            TREMOLO -> "Tremolo"
            CHORUS -> "Chorus"
            DELAY -> "Delay"
            REVERB -> "Reverb"
            LIMITER -> "Limitador"
        }
    }
}
