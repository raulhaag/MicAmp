package ar.rulosoft.micamp.DPS

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Process
import ar.rulosoft.micamp.data.EffectType
import ar.rulosoft.micamp.tools.updateWavHeader
import ar.rulosoft.micamp.tools.writeWavHeader
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.abs

class AudioEngine(
    val context: Context,
    val inputDevice: AudioDeviceInfo,
    val outputDevice: AudioDeviceInfo,
    val getVolume: () -> Float,
    
    // Order
    val getEffectOrder: () -> List<EffectType>,

    val getDistortion: () -> Float,
    val getEqGains: () -> FloatArray,
    val isDistortionEnabled: () -> Boolean,
    val isEqEnabled: () -> Boolean,
    
    // Reverb
    val getReverbParams: () -> FloatArray, // [mix, size]
    val isReverbEnabled: () -> Boolean,
    
    // Compressor
    val getCompressorParams: () -> FloatArray, // [threshold, ratio, makeup]
    val isCompressorEnabled: () -> Boolean,
    
    // Tremolo
    val getTremoloParams: () -> FloatArray, // [depth, rate]
    val isTremoloEnabled: () -> Boolean,
    
    // Chorus
    val getChorusParams: () -> FloatArray, // [rate, depth, mix]
    val isChorusEnabled: () -> Boolean,
    
    // Delay
    val getDelayParams: () -> FloatArray, // [time(s), feedback(0-1), mix(0-1)]
    val isDelayEnabled: () -> Boolean,
    
    // Noise Gate
    val getNoiseGateThreshold: () -> Float,
    val isNoiseGateEnabled: () -> Boolean,

    // Flanger
    val getFlangerParams: () -> FloatArray, // [rate, depth, mix, feedback]
    val isFlangerEnabled: () -> Boolean,

    // Phaser
    val getPhaserParams: () -> FloatArray, // [rate, depth, mix, feedback]
    val isPhaserEnabled: () -> Boolean,

    // Bitcrusher
    val getBitcrusherParams: () -> FloatArray, // [depth, rate, mix]
    val isBitcrusherEnabled: () -> Boolean,

    // Limiter
    val getLimiterThreshold: () -> Float,
    val isLimiterEnabled: () -> Boolean,

    // AutoWah
    val getAutoWahParams: () -> FloatArray, // [depth, rate, mix, resonance]
    val isAutoWahEnabled: () -> Boolean,
    
    val onRecordingError: (String) -> Unit,
    val onRecordingSaved: (String) -> Unit,
    val onRecordingProgress: (Long) -> Unit,
    val onVisualizerData: (FloatArray) -> Unit,
    val saveDir: Uri?
) {
    var onRecordingSavedUI: ((String) -> Unit)? = null
    
    private val isRunning = AtomicBoolean(false)
    private val isRecording = AtomicBoolean(false)
    private var audioThread: Thread? = null
    private var recordingThread: Thread? = null
    private val recordingQueue = ConcurrentLinkedQueue<ShortArray>()

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)
        audioThread = thread(start = true) { Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO); runLoop() }
        recordingThread = thread(start = true) { Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND); runRecordingLoop() }
    }

    fun stop() {
        isRunning.set(false)
        isRecording.set(false)
        try { audioThread?.join(1000); recordingThread?.join(1000) } catch (e: Exception) { e.printStackTrace() }
    }

    fun setRecording(record: Boolean) { isRecording.set(record) }

    private fun runLoop() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 48000
        val minBufIn = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val minBufOut = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        
        var record: AudioRecord? = null; var track: AudioTrack? = null
        try {
            val audioSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) MediaRecorder.AudioSource.UNPROCESSED else MediaRecorder.AudioSource.MIC
            record = AudioRecord.Builder().setAudioSource(audioSource).setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_IN_MONO).build()).setBufferSizeInBytes(maxOf(minBufIn, 1024)).build()
            record.preferredDevice = inputDevice
            val trackBuilder = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(maxOf(minBufOut, 1024)).setTransferMode(AudioTrack.MODE_STREAM)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            track = trackBuilder.build(); track.preferredDevice = outputDevice

            val chunkSize = minBufIn / 2; val chunkBuffer = ShortArray(chunkSize)
            
            // EQ Setup
            val eqFilters = Array(6) { Biquad(0, sampleRate.toFloat()) }
            val freqs = floatArrayOf(60f, 230f, 910f, 3000f, 14000f, 20000f)
            for (i in 0 until 6) eqFilters[i].update(freqs[i], 0f, 1.0f)
            
            // Delay Setup
            val maxDelaySeconds = 2.0f
            val maxDelaySamples = (sampleRate * maxDelaySeconds).toInt()
            val delayBuffer = FloatArray(maxDelaySamples)
            var delayWritePos = 0
            
            // New Effects Setup
            val reverb = Reverb(sampleRate)
            val compressor = Compressor(sampleRate)
            val tremolo = Tremolo(sampleRate)
            val chorus = Chorus(sampleRate)
            val noiseGate = NoiseGate(sampleRate)
            val flanger = Flanger(sampleRate)
            val phaser = Phaser(sampleRate)
            val bitcrusher = Bitcrusher(sampleRate)
            val limiter = Limiter(sampleRate)
            val autoWah = AutoWah(sampleRate)

            var framesSinceLastVis = 0; val visBuffer = FloatArray(100)

            record.startRecording(); track.play()
            while (isRunning.get()) {
                val read = record.read(chunkBuffer, 0, chunkBuffer.size)
                if (read > 0) {
                    val vol = getVolume()
                    val distAmt = getDistortion()
                    val eqGains = getEqGains()
                    
                    val effectOrder = getEffectOrder()
                    
                    val distortionEnabled = isDistortionEnabled()
                    val eqEnabled = isEqEnabled()
                    val delayEnabled = isDelayEnabled()
                    val reverbEnabled = isReverbEnabled()
                    val compressorEnabled = isCompressorEnabled()
                    val tremoloEnabled = isTremoloEnabled()
                    val chorusEnabled = isChorusEnabled()
                    val noiseGateEnabled = isNoiseGateEnabled()
                    val flangerEnabled = isFlangerEnabled()
                    val phaserEnabled = isPhaserEnabled()
                    val bitcrusherEnabled = isBitcrusherEnabled()
                    val limiterEnabled = isLimiterEnabled()
                    val autoWahEnabled = isAutoWahEnabled()

                    // Params snapshots
                    val delayParams = if(delayEnabled) getDelayParams() else floatArrayOf(0f,0f,0f)
                    val reverbParams = if(reverbEnabled) getReverbParams() else floatArrayOf(0f,0f) // mix, size
                    val compParams = if(compressorEnabled) getCompressorParams() else floatArrayOf(1f,1f,1f) // thresh, ratio, makeup
                    val tremParams = if(tremoloEnabled) getTremoloParams() else floatArrayOf(0f,0f) // depth, rate
                    val chorusParams = if(chorusEnabled) getChorusParams() else floatArrayOf(0f,0f,0f) // rate, depth, mix
                    val noiseGateThreshold = if(noiseGateEnabled) getNoiseGateThreshold() else 0f
                    val flangerParams = if(flangerEnabled) getFlangerParams() else floatArrayOf(0f,0f,0f,0f) // rate, depth, mix, feedback
                    val phaserParams = if(phaserEnabled) getPhaserParams() else floatArrayOf(0f,0f,0f,0f) // rate, depth, mix, feedback
                    val bitcrusherParams = if(bitcrusherEnabled) getBitcrusherParams() else floatArrayOf(0f,0f,0f) // depth, rate, mix
                    val limiterThreshold = if(limiterEnabled) getLimiterThreshold() else 1f
                    val autoWahParams = if(autoWahEnabled) getAutoWahParams() else floatArrayOf(0f,0f,0f,0f) // depth, rate, mix, resonance


                    if (eqEnabled) {
                         for (i in 0 until 6) eqFilters[i].update(freqs[i], eqGains[i], 1.4f)
                    }

                    for (i in 0 until read) {
                        var input = chunkBuffer[i].toFloat() / 32768f
                        
                        for (effect in effectOrder) {
                            when (effect) {
                                EffectType.NOISE_GATE -> if (noiseGateEnabled) input = noiseGate.process(input, noiseGateThreshold)
                                EffectType.COMPRESSOR -> if (compressorEnabled) input = compressor.process(input, compParams[0], compParams[1], compParams[2])
                                EffectType.AUTO_WAH -> if (autoWahEnabled) input = autoWah.process(input, autoWahParams[0], autoWahParams[1], autoWahParams[2], autoWahParams[3])
                                EffectType.BITCRUSHER -> if (bitcrusherEnabled) input = bitcrusher.process(input, bitcrusherParams[0], bitcrusherParams[1], bitcrusherParams[2])
                                EffectType.EQ -> if (eqEnabled) for (f in eqFilters) input = f.process(input)
                                EffectType.DISTORTION -> if (distortionEnabled && distAmt > 0.01f) { 
                                    val drive = 1f + distAmt * 20f; val x = input * drive; input = (x / (1f + abs(x))) 
                                }
                                EffectType.PHASER -> if (phaserEnabled) input = phaser.process(input, phaserParams[0], phaserParams[1], phaserParams[3], phaserParams[2])
                                EffectType.FLANGER -> if (flangerEnabled) input = flanger.process(input, flangerParams[0], flangerParams[1], flangerParams[2], flangerParams[3])
                                EffectType.TREMOLO -> if (tremoloEnabled) input = tremolo.process(input, tremParams[0], tremParams[1])
                                EffectType.CHORUS -> if (chorusEnabled) input = chorus.process(input, chorusParams[0], chorusParams[1], chorusParams[2])
                                EffectType.DELAY -> if (delayEnabled) {
                                    val delaySamples = (delayParams[0] * sampleRate).toInt().coerceIn(0, maxDelaySamples - 1)
                                    var delayReadPos = delayWritePos - delaySamples
                                    if (delayReadPos < 0) delayReadPos += maxDelaySamples
                                    
                                    val delayedSignal = delayBuffer[delayReadPos]
                                    delayBuffer[delayWritePos] = input + delayedSignal * delayParams[1] // Feedback
                                    
                                    input = input + delayedSignal * delayParams[2] // Mix
                                    
                                    delayWritePos++
                                    if (delayWritePos >= maxDelaySamples) delayWritePos = 0
                                } else {
                                     delayBuffer[delayWritePos] = input
                                     delayWritePos++
                                     if (delayWritePos >= maxDelaySamples) delayWritePos = 0
                                }
                                EffectType.REVERB -> if (reverbEnabled) input = reverb.process(input, reverbParams[0], reverbParams[1])
                                EffectType.LIMITER -> if (limiterEnabled) input = limiter.process(input, limiterThreshold)
                            }
                        }

                        input *= vol
                        var s = (input * 32767f).toInt(); if (s > 32767) s = 32767; if (s < -32768) s = -32768
                        chunkBuffer[i] = s.toShort()
                    }
                    track.write(chunkBuffer, 0, read)
                    if (isRecording.get()) recordingQueue.offer(chunkBuffer.copyOfRange(0, read))
                    
                    framesSinceLastVis++
                    if (framesSinceLastVis >= 2) {
                        framesSinceLastVis = 0; val step = read / 100f
                        if (step > 0) {
                            for (j in 0 until 100) { val idx = (j * step).toInt().coerceIn(0, read-1); visBuffer[j] = chunkBuffer[idx] / 32768f }
                            val dataToSend = visBuffer.clone()
                            android.os.Handler(android.os.Looper.getMainLooper()).post { onVisualizerData(dataToSend) }
                        }
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() } finally { try { track?.stop(); track?.release() } catch (e: Exception) {}; try { record?.stop(); record?.release() } catch (e: Exception) {} }
    }

    private fun runRecordingLoop() {
        var fos: FileOutputStream? = null; var currentFile: File? = null; var payloadSize = 0L; var startTime = 0L
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 48000

        while (isRunning.get()) {
            if (isRecording.get() && fos == null) {
                try {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC) // Temp
                    currentFile = File(dir, "MicAmp_$timeStamp.wav")
                    fos = FileOutputStream(currentFile)
                    writeWavHeader(fos!!, sampleRate, 0); payloadSize = 0; startTime = System.currentTimeMillis()
                } catch (e: Exception) { isRecording.set(false) }
            }
            if (!isRecording.get() && fos != null) {
                try {
                    fos.close(); updateWavHeader(currentFile!!, payloadSize.toInt(), sampleRate)
                    val path = currentFile.absolutePath
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onRecordingSavedUI?.invoke(path) }
                    fos = null; currentFile = null
                } catch (e: Exception) { e.printStackTrace() }
            }
            val chunk = recordingQueue.poll()
            if (chunk != null && fos != null) {
                try {
                    val bb = ByteBuffer.allocate(chunk.size * 2); bb.order(ByteOrder.LITTLE_ENDIAN); for (s in chunk) bb.putShort(s)
                    fos.write(bb.array()); payloadSize += chunk.size * 2
                    val elapsed = (System.currentTimeMillis() - startTime) / 1000
                    android.os.Handler(android.os.Looper.getMainLooper()).post { onRecordingProgress(elapsed) }
                } catch (e: Exception) {}
            } else { if (chunk == null) try { Thread.sleep(2) } catch (e: InterruptedException) {} }
        }
        if (fos != null) try { fos.close() } catch (e: Exception) {}
    }
}
