package ar.rulosoft.micamp.data

import org.json.JSONArray
import org.json.JSONObject

data class DspConfig(
    val name: String,
    val volume: Float,
    
    val distortion: Float, val isDistortionEnabled: Boolean,
    
    val eqBands: List<Float>, val isEqEnabled: Boolean,
    
    val delayTime: Float, val delayFeedback: Float, val delayMix: Float, val isDelayEnabled: Boolean,
    
    val reverbMix: Float, val reverbSize: Float, val isReverbEnabled: Boolean,
    
    val compThreshold: Float, val compRatio: Float, val compMakeup: Float, val isCompressorEnabled: Boolean,
    
    val tremDepth: Float, val tremRate: Float, val isTremoloEnabled: Boolean,
    
    val chorusRate: Float, val chorusDepth: Float, val chorusMix: Float, val isChorusEnabled: Boolean,
    
    val noiseGateThreshold: Float = 0.05f, val isNoiseGateEnabled: Boolean = false,
    
    val flangerRate: Float = 0.5f, val flangerDepth: Float = 2.0f, val flangerFeedback: Float = 0.5f, val flangerMix: Float = 0.5f, val isFlangerEnabled: Boolean = false,

    // Nuevos efectos
    val phaserRate: Float = 1.0f, val phaserDepth: Float = 0.5f, val phaserFeedback: Float = 0.5f, val phaserMix: Float = 0.5f, val isPhaserEnabled: Boolean = false,
    
    val bitcrusherDepth: Float = 0.5f, val bitcrusherRate: Float = 0.1f, val bitcrusherMix: Float = 1.0f, val isBitcrusherEnabled: Boolean = false,
    
    val limiterThreshold: Float = 0.95f, val isLimiterEnabled: Boolean = false,
    
    val autoWahDepth: Float = 0.5f, val autoWahRate: Float = 0.5f, val autoWahMix: Float = 0.5f, val autoWahResonance: Float = 0.5f, val isAutoWahEnabled: Boolean = false
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("name", name)
        json.put("volume", volume.toDouble())
        
        json.put("distortion", distortion.toDouble())
        json.put("isDistortionEnabled", isDistortionEnabled)
        
        val eqArray = JSONArray()
        eqBands.forEach { eqArray.put(it.toDouble()) }
        json.put("eqBands", eqArray)
        json.put("isEqEnabled", isEqEnabled)
        
        json.put("delayTime", delayTime.toDouble())
        json.put("delayFeedback", delayFeedback.toDouble())
        json.put("delayMix", delayMix.toDouble())
        json.put("isDelayEnabled", isDelayEnabled)
        
        json.put("reverbMix", reverbMix.toDouble())
        json.put("reverbSize", reverbSize.toDouble())
        json.put("isReverbEnabled", isReverbEnabled)
        
        json.put("compThreshold", compThreshold.toDouble())
        json.put("compRatio", compRatio.toDouble())
        json.put("compMakeup", compMakeup.toDouble())
        json.put("isCompressorEnabled", isCompressorEnabled)
        
        json.put("tremDepth", tremDepth.toDouble())
        json.put("tremRate", tremRate.toDouble())
        json.put("isTremoloEnabled", isTremoloEnabled)
        
        json.put("chorusRate", chorusRate.toDouble())
        json.put("chorusDepth", chorusDepth.toDouble())
        json.put("chorusMix", chorusMix.toDouble())
        json.put("isChorusEnabled", isChorusEnabled)
        
        json.put("noiseGateThreshold", noiseGateThreshold.toDouble())
        json.put("isNoiseGateEnabled", isNoiseGateEnabled)
        
        json.put("flangerRate", flangerRate.toDouble())
        json.put("flangerDepth", flangerDepth.toDouble())
        json.put("flangerFeedback", flangerFeedback.toDouble())
        json.put("flangerMix", flangerMix.toDouble())
        json.put("isFlangerEnabled", isFlangerEnabled)
        
        // Nuevos efectos JSON
        json.put("phaserRate", phaserRate.toDouble())
        json.put("phaserDepth", phaserDepth.toDouble())
        json.put("phaserFeedback", phaserFeedback.toDouble())
        json.put("phaserMix", phaserMix.toDouble())
        json.put("isPhaserEnabled", isPhaserEnabled)
        
        json.put("bitcrusherDepth", bitcrusherDepth.toDouble())
        json.put("bitcrusherRate", bitcrusherRate.toDouble())
        json.put("bitcrusherMix", bitcrusherMix.toDouble())
        json.put("isBitcrusherEnabled", isBitcrusherEnabled)
        
        json.put("limiterThreshold", limiterThreshold.toDouble())
        json.put("isLimiterEnabled", isLimiterEnabled)
        
        json.put("autoWahDepth", autoWahDepth.toDouble())
        json.put("autoWahRate", autoWahRate.toDouble())
        json.put("autoWahMix", autoWahMix.toDouble())
        json.put("autoWahResonance", autoWahResonance.toDouble())
        json.put("isAutoWahEnabled", isAutoWahEnabled)
        
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): DspConfig {
            val json = JSONObject(jsonStr)
            val name = json.optString("name", "Preset")
            val volume = json.optDouble("volume", 1.0).toFloat()
            
            val distortion = json.optDouble("distortion", 0.0).toFloat()
            val isDistortionEnabled = json.optBoolean("isDistortionEnabled", false)
            
            val eqArray = json.optJSONArray("eqBands")
            val eqBands = mutableListOf<Float>()
            if (eqArray != null) {
                for (i in 0 until eqArray.length()) {
                    eqBands.add(eqArray.getDouble(i).toFloat())
                }
            } else {
                repeat(6) { eqBands.add(0f) }
            }
            val isEqEnabled = json.optBoolean("isEqEnabled", true)
            
            return DspConfig(
                name = name,
                volume = volume,
                distortion = distortion, isDistortionEnabled = isDistortionEnabled,
                eqBands = eqBands, isEqEnabled = isEqEnabled,
                
                delayTime = json.optDouble("delayTime", 0.3).toFloat(),
                delayFeedback = json.optDouble("delayFeedback", 0.4).toFloat(),
                delayMix = json.optDouble("delayMix", 0.3).toFloat(),
                isDelayEnabled = json.optBoolean("isDelayEnabled", false),
                
                reverbMix = json.optDouble("reverbMix", 0.3).toFloat(),
                reverbSize = json.optDouble("reverbSize", 0.5).toFloat(),
                isReverbEnabled = json.optBoolean("isReverbEnabled", false),
                
                compThreshold = json.optDouble("compThreshold", 0.5).toFloat(),
                compRatio = json.optDouble("compRatio", 4.0).toFloat(),
                compMakeup = json.optDouble("compMakeup", 1.0).toFloat(),
                isCompressorEnabled = json.optBoolean("isCompressorEnabled", false),
                
                tremDepth = json.optDouble("tremDepth", 0.5).toFloat(),
                tremRate = json.optDouble("tremRate", 5.0).toFloat(),
                isTremoloEnabled = json.optBoolean("isTremoloEnabled", false),
                
                chorusRate = json.optDouble("chorusRate", 1.0).toFloat(),
                chorusDepth = json.optDouble("chorusDepth", 2.0).toFloat(),
                chorusMix = json.optDouble("chorusMix", 0.5).toFloat(),
                isChorusEnabled = json.optBoolean("isChorusEnabled", false),
                
                noiseGateThreshold = json.optDouble("noiseGateThreshold", 0.05).toFloat(),
                isNoiseGateEnabled = json.optBoolean("isNoiseGateEnabled", false),
                
                flangerRate = json.optDouble("flangerRate", 0.5).toFloat(),
                flangerDepth = json.optDouble("flangerDepth", 2.0).toFloat(),
                flangerFeedback = json.optDouble("flangerFeedback", 0.5).toFloat(),
                flangerMix = json.optDouble("flangerMix", 0.5).toFloat(),
                isFlangerEnabled = json.optBoolean("isFlangerEnabled", false),

                phaserRate = json.optDouble("phaserRate", 1.0).toFloat(),
                phaserDepth = json.optDouble("phaserDepth", 0.5).toFloat(),
                phaserFeedback = json.optDouble("phaserFeedback", 0.5).toFloat(),
                phaserMix = json.optDouble("phaserMix", 0.5).toFloat(),
                isPhaserEnabled = json.optBoolean("isPhaserEnabled", false),
                
                bitcrusherDepth = json.optDouble("bitcrusherDepth", 0.5).toFloat(),
                bitcrusherRate = json.optDouble("bitcrusherRate", 0.1).toFloat(),
                bitcrusherMix = json.optDouble("bitcrusherMix", 1.0).toFloat(),
                isBitcrusherEnabled = json.optBoolean("isBitcrusherEnabled", false),
                
                limiterThreshold = json.optDouble("limiterThreshold", 0.95).toFloat(),
                isLimiterEnabled = json.optBoolean("isLimiterEnabled", false),
                
                autoWahDepth = json.optDouble("autoWahDepth", 0.5).toFloat(),
                autoWahRate = json.optDouble("autoWahRate", 0.5).toFloat(),
                autoWahMix = json.optDouble("autoWahMix", 0.5).toFloat(),
                autoWahResonance = json.optDouble("autoWahResonance", 0.5).toFloat(),
                isAutoWahEnabled = json.optBoolean("isAutoWahEnabled", false)
            )
        }
    }
}
