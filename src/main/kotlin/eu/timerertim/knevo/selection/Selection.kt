@file:JvmName("Selection")

package eu.timerertim.knevo.selection

import eu.timerertim.knevo.Genome
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.nextDown
import kotlin.math.pow
import kotlin.random.Random

sealed interface SelectionFunction : Serializable {
    operator fun <T : Genome> invoke(candidates: Collection<T>): T
}


data class Power @JvmOverloads constructor(val exponent: Double = 5.0) : SelectionFunction {
    constructor(exponent: Int) : this(exponent.toDouble())

    override fun <T : Genome> invoke(candidates: Collection<T>): T {
        val index = (Random.nextDouble().pow(exponent) * candidates.size).toInt()
        return candidates.elementAt(index)
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}

data class Tournament @JvmOverloads constructor(val size: Int? = null, val probability: Float = 0.5F) :
    SelectionFunction {
    override fun <T : Genome> invoke(candidates: Collection<T>): T {
        val tournament = if (size != null) {
            require(size <= candidates.size) {
                "Tournament size must be lower than candidates size to be a valid SelectionFunction"
            }

            val individuals = mutableListOf<T>()
            repeat(size) {
                individuals += candidates.random()
            }
            individuals.toSortedSet { compare, to -> -compare.compareTo(to) }
        } else {
            candidates
        }

        for (candidate in tournament) {
            if (Random.nextFloat() < probability) return candidate
        }
        return tournament.last()
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}

data class FitnessProportionate private constructor(
    private val buffer: ConcurrentHashMap<Collection<Genome>, Pair<Double, Float>>
) : SelectionFunction {
    constructor() : this(ConcurrentHashMap())

    fun reset() {
        buffer.clear()
    }

    override fun <T : Genome> invoke(candidates: Collection<T>): T {
        val (totalScore, minimumScore) = buffer.getOrPut(candidates) {
            var totalScore = 0.0
            var minimumScore = Float.POSITIVE_INFINITY
            for (candidate in candidates) {
                minimumScore = min(candidate.score, minimumScore)
                totalScore += candidate.score
            }

            minimumScore = minimumScore.nextDown()
            totalScore += minimumScore * candidates.size
            Pair(totalScore, minimumScore)
        }


        val random = Random.nextDouble(totalScore).toFloat()
        var value = 0F
        for (candidate in candidates) {
            value += candidate.score - minimumScore
            if (random < value) return candidate
        }

        return candidates.random()
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}
