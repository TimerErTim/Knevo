package com.evo.NEAT

import com.evo.NEAT.config.Config
import com.evo.NEAT.config.Seed

import java.util.ArrayList
import java.util.Collections
import java.util.Random

class Species : Comparable<Species> {

    val genomes = ArrayList<Genome>()

    // todo: check if this assignment ever matters
    var topFitness = 0f
        get() {
            this.topFitness = topGenome.fitness
            this.topFitness = topGenome.fitness
            return field
        }

    // todo: convert to functions
    var staleness = 0

    private val random = Seed.random

    val totalAdjustedFitness: Float
        get() = genomes.sumByDouble { it.adjustedFitness.toDouble() }.toFloat()

    val topGenome: Genome
        get() {
            sortGenomes()
            return genomes[0]
        }


    constructor() : super() {}

    constructor(top: Genome) : super() {
        this.genomes.add(top)
    }

    fun calculateGenomeAdjustedFitness() {
        for (g in genomes) {
            g.adjustedFitness = g.fitness / genomes.size
        }
    }


    private fun sortGenomes() {
        genomes.sortDescending()
    }

    fun removeWeakGenomes(allButOne: Boolean) {
        sortGenomes()
        var surviveCount = 1
        if (!allButOne)
            surviveCount = Math.ceil((genomes.size / 2f).toDouble()).toInt()

        val survivedGenomes = ArrayList<Genome>()
        for (i in 0 until surviveCount) {
            survivedGenomes.add(Genome(genomes[i]))
        }

        genomes.clear()
        genomes.addAll(survivedGenomes)
    }

    @Deprecated("")
    fun removeWeakGenome(childrenToRemove: Int) {
        sortGenomes()
        val survived = ArrayList<Genome>()
        for (i in 0 until genomes.size - childrenToRemove) {
            survived.add(genomes[i])
        }

        genomes.clear()
        genomes.addAll(survived)
    }


    fun breedChild(): Genome {
        var child: Genome
        if (random.nextFloat() < Config.CROSSOVER_CHANCE) {
            val g1 = genomes[random.nextInt(genomes.size)]
            val g2 = genomes[random.nextInt(genomes.size)]
            child = Genome.crossOver(g1, g2)
        } else {
            val g1 = genomes[random.nextInt(genomes.size)]
            child = g1
        }
        child = Genome(child)
        child.Mutate()

        return child
    }

    override operator fun compareTo(other: Species): Int {
        return topFitness.compareTo(other.topFitness)
    }
}
