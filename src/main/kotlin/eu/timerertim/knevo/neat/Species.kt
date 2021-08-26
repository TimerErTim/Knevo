package eu.timerertim.knevo.neat

import eu.timerertim.knevo.neat.config.Defaults
import eu.timerertim.knevo.neat.config.Seed

class Species() : Comparable<Species> {

    val genomes = ArrayList<Genome>()

    var topFitness = 0f

    val isStale: Boolean
        get() = staleness < Defaults.STALE_SPECIES

    private var staleness = 0

    fun resetStaleness() {
        staleness = 0
    }

    fun increaseStaleness() {
        staleness++
    }

    private val random = Seed.random

    val totalAdjustedFitness: Float
        get() = genomes.sumOf { it.adjustedFitness.toDouble() }.toFloat()

    val topGenome: Genome
        get() = genomes.maxByOrNull { it.fitness }!!

    constructor(top: Genome) : this() {
        genomes.add(top)
    }

    fun calculateGenomeAdjustedFitness() {
        for (genome in genomes) {
            genome.adjustFitness(genomes.size)
        }
    }

    fun removeWeakGenomes() {
        genomes.sortDescending()

        val count = Math.ceil((genomes.size / 2f).toDouble()).toInt()
        val top = genomes.take(count)

        genomes.clear()
        genomes.addAll(top)
    }


    fun breedChild(): Genome {
        val child = if (random.nextFloat() < Defaults.CROSSOVER_CHANCE) {
            Genome.crossOver(genomes.random(Seed.random), genomes.random(Seed.random))
        } else {
            genomes.random(Seed.random).clone()
        }

        child.mutate()
        return child
    }

    override operator fun compareTo(other: Species): Int {
        return topFitness.compareTo(other.topFitness)
    }
}
