package eu.timerertim.knevo

/**
 * Environments determine the fitness of a List of [Genome]s.
 */
interface Environment<in G : Genome> {
    /**
     * Evaluates the fitness of the given [population]. Depending on the [Population] this function could be run in
     * parallel and only with a part of the original population, so it should be threadsafe.
     */
    fun evaluateFitness(population: List<G>)
}
