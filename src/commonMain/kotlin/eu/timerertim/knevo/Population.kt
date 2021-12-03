package eu.timerertim.knevo

import java.io.Serializable

/**
 * A [Population] is a [Collection] of [Genome]s. It can be used to train and evolve Genomes. For this one should invoke
 * [evaluateFitness] to determine each Genome's fitness and then [breedNewGeneration] to produce the next [generation].
 * The shorthand form is [evolve].
 */
interface Population<out G : Genome> : Collection<G>, Serializable {
    /**
     * This number keeps track of the current [generation]. It normally is increased upon invoking [breedNewGeneration].
     */
    val generation: Long


    /**
     * The [fitness][Genome.fitness] of the [best Genome][topGenome] in this [Population].
     */
    val topFitness get() = topGenome.fitness

    /**
     * The [score][Genome.score] of the [best Genome][topGenome] in this [Population].
     */
    val topScore get() = topGenome.score

    /**
     * The best performing [Genome] according to [score][Genome.score].
     */
    val topGenome get() = maxOrNull()!!

    /**
     * Evolves this [Population] using a given [environment]. It invokes [evaluateFitness] and then
     * [breedNewGeneration].
     */
    fun evolve(environment: Environment<G>) {
        evaluateFitness(environment)
        breedNewGeneration()
    }


    /**
     * Evaluates the [fitness][Genome.fitness] of every single [Genome] in this [Population] by using the given
     * [environment].
     */
    fun evaluateFitness(environment: Environment<G>)

    /**
     * Breeds a new generation based on the [evaluated Fitness][evaluateFitness]. Typically, increases [generation].
     */
    fun breedNewGeneration()
}
