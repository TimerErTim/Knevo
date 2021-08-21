package eu.timerertim.kneat

/**
 * assign Fitness to each genome
 */
interface Environment {

    fun evaluateFitness(population: List<Genome>)

}
