package com.evo.NEAT

import com.evo.NEAT.config.NEAT_Config

import java.util.ArrayList
import java.util.Collections
import java.util.Random

/**
 * Created by vishnu on 7/1/17.
 */
class Species : Comparable<Species> {
    var genomes = ArrayList<Genome>()
        private set
    var topFitness = 0f
        get() {
            this.topFitness = topGenome.fitness
            this.topFitness = topGenome.fitness
            return field
        }
    var staleness = 0
    internal var rand = Random()

    val totalAdjustedFitness: Float
        get() {
            var totalAdjustedFitness = 0f
            for (g in genomes) {
                totalAdjustedFitness += g.adjustedFitness
            }

            return totalAdjustedFitness
        }

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
        //sort internally genomes
        Collections.sort(genomes, Collections.reverseOrder())
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
        genomes = survivedGenomes
    }

    @Deprecated("")
    fun removeWeakGenome(childrenToRemove: Int) {
        sortGenomes()
        val survived = ArrayList<Genome>()
        for (i in 0 until genomes.size - childrenToRemove) {
            survived.add(genomes[i])
        }
        genomes = survived
    }


    fun breedChild(): Genome {
        var child: Genome
        if (rand.nextFloat() < NEAT_Config.CROSSOVER_CHANCE) {
            val g1 = genomes[rand.nextInt(genomes.size)]
            val g2 = genomes[rand.nextInt(genomes.size)]
            child = Genome.crossOver(g1, g2)
        } else {
            val g1 = genomes[rand.nextInt(genomes.size)]
            child = g1
        }
        child = Genome(child)
        child.Mutate()

        return child
    }

    override operator fun compareTo(s: Species): Int {
        val top = topFitness
        val otherTop = s.topFitness

        return if (top == otherTop)
            0
        else if (top > otherTop)
            1
        else
            -1
    }
}
