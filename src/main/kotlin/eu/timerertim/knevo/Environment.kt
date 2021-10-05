package eu.timerertim.knevo

/**
 * Environments determine the fitness of multiple [Genome]s.
 */
interface Environment<in G : Genome> {
    fun evaluateFitness(population: List<G>)
}
