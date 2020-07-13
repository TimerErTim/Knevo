package com.evo.NEAT

/**
 * assign Fitness to each genome
 */
interface Environment {

    fun evaluateFitness(population: List<Genome>)

}
