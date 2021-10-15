@file:JvmName("Selection")

package eu.timerertim.knevo.selection

import eu.timerertim.knevo.Genome
import eu.timerertim.knevo.Population
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.nextDown
import kotlin.math.pow
import kotlin.random.Random

/**
 * A [SelectionFunction] is a generic function which selects one [Genome] from a collection of Genomes. It is typically
 * used in [Population]s to determine the next generation.
 *
 * Every SelectionFunction expects the Genomes to be sorted descending according to their [score][Genome.score]
 */
sealed interface SelectionFunction : Serializable {
    /**
     * Selects a [Genome] of type [G] from the given [candidates].
     */
    operator fun <G : Genome> invoke(candidates: Collection<G>): G
}

/**
 * [Power] is a [SelectionFunction] which returns one of the [Genome]s with an exponential distribution. The [exponent]
 * can be manually configured.
 */
data class Power @JvmOverloads constructor(val exponent: Double = 5.0) : SelectionFunction {
    /**
     * [Power] is a [SelectionFunction] which returns one of the [Genome]s with an exponential distribution. The
     * [exponent] can be manually configured.
     */
    constructor(exponent: Int) : this(exponent.toDouble())

    /**
     * Selects a [Genome] of type [G] from the given [candidates] by using an exponential distribution. The method
     * requires candidates to be sorted descending.
     */
    override fun <G : Genome> invoke(candidates: Collection<G>): G {
        val index = (Random.nextDouble().pow(exponent) * candidates.size).toInt()
        return candidates.elementAt(index)
    }

    private companion object {
        private const val serialVersionUID = 0L
    }
}

/**
 * [Tournament] is a [SelectionFunction] and creates a pool of given [size] filled with [Genome]s randomly picked from
 * the given ones. That pool is then sorted descending and every Genome one after another
 * has a [probability] of being selected.
 *
 * The size can be null, in which case all the given Genomes are used as pool.
 */
data class Tournament @JvmOverloads constructor(val size: Int? = null, val probability: Float = 0.5F) :
    SelectionFunction {
    /**
     * Selects a [Genome] of type [G] from the given [candidates] by using the [Tournament] method.
     */
    override fun <G : Genome> invoke(candidates: Collection<G>): G {
        val tournament = if (size != null) {
            val individuals = mutableListOf<G>()
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

    private companion object {
        private const val serialVersionUID = 0L
    }
}

/**
 * [FitnessProportionate] is a [SelectionFunction] which selects [Genome]s with a distribution proportionate to their
 * [score][Genome.score]. If at least one of the scores are negative, the scores are normalized by applying a uniform
 * compensation value, so that the lowest score is effectively at least a bit above 0.
 *
 * The internal [buffer] is used to only do the above calculation if a new [Collection] of Genomes is given. That buffer
 * has to be [reset]ted in case the Genomes themselves have changed but the Collection object has stayed the same.
 */
data class FitnessProportionate @JvmOverloads constructor(
    private val buffer: ConcurrentHashMap<Collection<Genome>, Pair<Double, Float>> = ConcurrentHashMap()
) : SelectionFunction {

    /**
     * Resets the internal [buffer]. This should be invoked when the [Genome]s' [score][Genome.score] have been changed,
     * but are given using the same [Collection].
     */
    fun reset() {
        buffer.clear()
    }

    /**
     * Selects a [Genome] of type [G] from the given [candidates] by using the [FitnessProportionate] method.
     */
    override fun <G : Genome> invoke(candidates: Collection<G>): G {
        val (totalScore, minimumScore) = buffer.getOrPut(candidates) {
            var totalScore = 0.0
            var minimumScore = Float.POSITIVE_INFINITY
            for (candidate in candidates) {
                minimumScore = min(candidate.score, minimumScore)
                totalScore += candidate.score
            }

            minimumScore = if (minimumScore < 0) {
                minimumScore.nextDown().also {
                    totalScore -= it * candidates.size
                }
            } else {
                0F
            }
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

    private companion object {
        private const val serialVersionUID = 0L
    }
}
