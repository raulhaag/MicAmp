package ar.rulosoft.micamp.DPS

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class Biquad(val type: Int, val fs: Float) {
    var a0 = 0f; var a1 = 0f; var a2 = 0f; var b0 = 0f; var b1 = 0f; var b2 = 0f
    var x1 = 0f; var x2 = 0f; var y1 = 0f; var y2 = 0f
    fun update(freq: Float, gainDB: Float, Q: Float) {
        val w0 = 2.0 * Math.PI * freq / fs; val alpha = sin(w0) / (2.0 * Q); val A = 10.0.pow(gainDB / 40.0); val cosw0 = cos(w0)
        val b0_tmp = 1 + alpha * A; val b1_tmp = -2 * cosw0; val b2_tmp = 1 - alpha * A
        val a0_tmp = 1 + alpha / A; val a1_tmp = -2 * cosw0; val a2_tmp = 1 - alpha / A
        b0 = (b0_tmp / a0_tmp).toFloat(); b1 = (b1_tmp / a0_tmp).toFloat(); b2 = (b2_tmp / a0_tmp).toFloat()
        a1 = (a1_tmp / a0_tmp).toFloat(); a2 = (a2_tmp / a0_tmp).toFloat()
    }
    fun process(input: Float): Float {
        val output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1; x1 = input; y2 = y1; y1 = output; return output
    }
}