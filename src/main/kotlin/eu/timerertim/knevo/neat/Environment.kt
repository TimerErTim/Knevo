package eu.timerertim.knevo.neat

/**
 * assign Fitness to each genome
 */
interface Environment {

    fun evaluateFitness(population: List<Genome>)

}
