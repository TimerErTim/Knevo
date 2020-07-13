package com.evo.NEAT

import com.evo.NEAT.config.Config
import com.evo.NEAT.config.Seed

import java.util.ArrayList

class Species() : Comparable<Species> {

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
            genomes.sortDescending()


            
            return genomes[0]
        }

    constructor(top: Genome) : this() {
        genomes.add(top)
    }

    fun calculateGenomeAdjustedFitness() {
        for (g in genomes) {
            g.adjustedFitness = g.fitness / genomes.size
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
        var child: Genome = if (random.nextFloat() < Config.CROSSOVER_CHANCE) {
            val g1 = genomes[random.nextInt(genomes.size)]
            val g2 = genomes[random.nextInt(genomes.size)]
            Genome.crossOver(g1, g2)
        } else {
            val g1 = genomes[random.nextInt(genomes.size)]
            g1
        }
        child = child.clone()
        child.mutate()

        return child
    }

    override operator fun compareTo(other: Species): Int {
        return topFitness.compareTo(other.topFitness)
    }
}
