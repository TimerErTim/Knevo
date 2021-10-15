package eu.timerertim.knevo.shared.multiplexer

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

class Multiplexer(val addressBits: Int) {
    private val random = ThreadLocalRandom.current()

    val registerBits = 1 shl addressBits
    val totalBits = registerBits + addressBits
    val randomInputs get() = BooleanArray(totalBits) { random.nextBoolean() }
    val allPossibleInputs by lazy {
        val result = Array(1 shl totalBits) { BooleanArray(totalBits) }

        for (i in result.indices) {
            val bitString = String.format("%${totalBits}s", Integer.toBinaryString(i)).replace(' ', '0')
            result[i] = bitString.toCharArray().map { it == '1' }.toBooleanArray()
        }

        result
    }

    operator fun invoke(inputs: BooleanArray): Boolean {
        if (inputs.size != totalBits) throw IllegalArgumentException("Input size does not match expected size")

        val addressBinary = inputs.copyOfRange(0, addressBits)
        val register = inputs.copyOfRange(addressBits, totalBits).apply { reverse() }

        val addressString = addressBinary.foldRight("") { left, right -> (if (left) "1" else "0") + right }
        val address = Integer.valueOf(addressString, 2)

        return register[address]
    }

    fun fitness(inputs: BooleanArray, prediction: Float): Float {
        val result = if (this(inputs)) 1F else 0F
        return 1 - abs(result - prediction)
    }

    companion object {
        @JvmStatic
        fun convertToFloat(inputs: BooleanArray) = inputs.map { if (it) 1F else 0F }.toFloatArray()
    }
}