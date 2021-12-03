package eu.timerertim.knevo

import java.io.Serializable

/**
 * A [Genome] actually is the phenotype of a neuroevolution algorithm. It can be invoked using a [FloatArray] as input
 * while returning its output as FloatArray. Furthermore, it provides common properties and functions, which are all
 * shared by different algorithm implementations.
 */
interface Genome : Comparable<Genome>, (FloatArray) -> FloatArray, Serializable {
    /**
     * The fitness evaluated by an [Environment].
     */
    var fitness: Float

    /**
     * The modified [fitness] which is used for comparing different [Genome]s.
     * In many cases this is equal to fitness, but keep in mind that depending on configuration and algorithm it might
     * differ.
     */
    val score: Float get() = fitness

    /**
     * If this is true upon next [invoke], this [Genome] is reset and forgets all its previous states.
     */
    var doReset: Boolean

    /**
     * Compares this and one [other] [Genome] based on their [score][Genome.score].
     */
    override operator fun compareTo(other: Genome) = this.score.compareTo(other.score)

    /**
     * Invokes this [Genome]. That means taking the given [input], processing it (typically running through an
     * ANN) and returning the result.
     */
    override fun invoke(input: FloatArray): FloatArray
}
