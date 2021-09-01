package eu.timerertim.knevo.activation

import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * An [ActivationFunction] is a function which takes a [Float] value and returns another Float depending on said input.
 * This can be literally any function, ranging from linear function to sinus function.
 */
typealias ActivationFunction = (Float) -> Float

/**
 * The [Sigmoid] [] [ActivationFunction] can be viewed [here](https://www.geogebra.org/m/QvSuH67g).
 * The [slope] (similar to "a" on linked webpage) can manually be specified to fit different needs.
 */
class Sigmoid @JvmOverloads constructor(private val slope: Float = 4.9F) : ActivationFunction {
    override fun invoke(x: Float) = (1 / (1 + exp(-slope * x)))
}

/**
 * The [Tanh] [] [ActivationFunction] is similar to the [Sigmoid] function, but its output range is (-1, 1) in contrast
 * to Sigmoid's (0, 1) range. The [slope] can be specified when a non-default value is needed.
 */
class Tanh @JvmOverloads constructor(private val slope: Float = 2F) : ActivationFunction {
    override fun invoke(x: Float) = (2 / (1 + exp(-slope * x))) - 1
}

/**
 * The [Sinus] [] [ActivationFunction] should be self-explanatory. Its output range is `[-1, 1]`.
 * The [compression] can be specified. It should be noted, that values < 1 result in a stretch instead.
 */
class Sinus @JvmOverloads constructor(private val compression: Float = 1F) : ActivationFunction {
    override fun invoke(x: Float) = sin(compression * x)
}

/**
 * The [Step] [] [ActivationFunction] is a threshold function with an output of either 1 or 0.
 * The [threshold] t (defaults to 0) defines the function in the following way:
 *
 * y = 1 if x >= t else 0
 */
class Step @JvmOverloads constructor(private val threshold: Float = 0F) : ActivationFunction {
    override fun invoke(x: Float) = if (x >= threshold) 1F else 0F
}

/**
 * The [Sign] [] [ActivationFunction] is a threshold function with an output of either 1 or -1.
 * The [threshold] t (defaults to 0) defines the function in the following way:
 *
 * y = 1 if x >= t else -1
 */
class Sign @JvmOverloads constructor(private val threshold: Float = 0F) : ActivationFunction {
    override fun invoke(x: Float) = if (x >= threshold) 1F else -1F
}

/**
 * The [Random] [] [ActivationFunction] returns a random value in the range of [-1, 1) regardless of input. The [random]
 * number generator can be set manually after initialization.
 */
class Random : ActivationFunction {
    var random = Random

    @Synchronized
    override fun invoke(x: Float) = random.nextFloat() * 2 - 1
}

/**
 * The [Relu] [] [ActivationFunction] returns either its input or 0, whatever is greater.
 */
class Relu : ActivationFunction {
    override fun invoke(x: Float) = if (x > 0F) x else 0F
}

/**
 * The [Selu] [] [ActivationFunction] is hard to describe, but you can find a good explanation
 * [here](https://mlfromscratch.com/activation-functions-explained/#selu).
 */
class Selu : ActivationFunction {
    companion object {
        private const val alpha = 1.6732632423543772848170429916717F
        private const val lambda = 1.0507009873554804934193349852946F
    }

    override fun invoke(x: Float) = lambda * if (x > 0F) x else alpha * exp(x) - alpha
}

/**
 * The [Silu] [] [ActivationFunction] is similar to the [Relu] function but uses the [Sigmoid] function.
 * Similarly, you can set the "slope" of the Sigmoid part using the [a] value. In this function it controls
 * the amount of leakage for inputs below 0.
 */
class Silu @JvmOverloads constructor(private val a: Float = 1F) : ActivationFunction {
    override fun invoke(x: Float) = (x / (1 + exp(-a * x)))
}

/**
 * The [Linear] [] [ActivationFunction] multiplies the input with the [gradient] (defaults to 1).
 */
class Linear @JvmOverloads constructor(private val gradient: Float = 1F) : ActivationFunction {
    override fun invoke(x: Float) = gradient * x
}

/**
 * This function returns a special [Linear] [] [ActivationFunction], which returns its input as output, called [Identity].
 */
fun Identity() = Linear()