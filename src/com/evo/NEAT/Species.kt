package com.evo.NEAT

import com.evo.NEAT.config.Config
import com.evo.NEAT.config.Seed

import java.util.ArrayList

class Species() : Comparable<Species> {

    val genomes = ArrayList<Genome>()

    var topFitness = 0f

    val isStale: Boolean
        get() = staleness < Config.STALE_SPECIES

    private var staleness = 0

    fun resetStaleness() {
        staleness = 0
    }

    fun increaseStaleness() {
        staleness++
    }

    private val random = Seed.random

    val totalAdjustedFitness: Float
        get() = genomes.sumByDouble { it.adjustedFitness.toDouble() }.toFloat()

    val topGenome: Genome
        get() = genomes.maxBy { it.fitness }!!

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
        val child = if (random.nextFloat() < Config.CROSSOVER_CHANCE) {
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
