package eu.timerertim.knevo

/**
 * [FitnessFunction]s determine the [fitness][Genome.fitness] of a given [Genome]. It can be used like an [Environment]
 * for training.
 */
interface FitnessFunction<in G : Genome> : Environment<G>, suspend (G) -> Float {
    override suspend fun evaluateFitness(population: List<G>) {
        population.forEach {
            it.fitness = this(it)
        }
    }

    companion object {
        /**
         * Creates a [FitnessFunction] from the given [function].
         */
        @JvmStatic
        @JvmName("from")
        operator fun <G : Genome> invoke(function: suspend (G) -> Float): FitnessFunction<G> =
            object : FitnessFunction<G>, suspend (G) -> Float by function {}
    }
}
