package com.evo.NEAT

import com.evo.NEAT.config.Config

import java.util.ArrayList
import java.util.Collections

class Pool {

    private var species = ArrayList<Species>()

    private var generations = 0
    private val topFitness: Float = 0.toFloat()
    private var poolStaleness = 0

    val topGenome: Genome
        get() = species.flatMap { it.genomes }.maxBy { it.fitness }!!


    fun initializePool() {
        for (i in 0 until Config.POPULATION) {
            addToSpecies(Genome())
        }
    }

    private fun addToSpecies(genome: Genome) {
        for (s in species) {
            if (s.genomes.size == 0)
                continue
            val g0 = s.genomes[0]

            if (Genome.isSameSpecies(genome, g0)) {
                s.genomes.add(genome)
                return
            }
        }
        val childSpecies = Species()
        childSpecies.genomes.add(genome)
        species.add(childSpecies)
    }

    fun evaluateFitness(environment: Environment) {
        val allGenome = ArrayList<Genome>()

        for (s in species) {
            for (g in s.genomes) {
                allGenome.add(g)
            }
        }

        environment.evaluateFitness(allGenome)
        rankGlobally()
    }

    // experimental
    private fun rankGlobally() {                // set fitness to rank
        val allGenome = ArrayList<Genome>()

        for (s in species) {
            for (g in s.genomes) {
                allGenome.add(g)
            }
        }
        allGenome.sort()

        for (i in allGenome.indices) {
            allGenome[i].points = allGenome[i].fitness      //TODO use adjustedFitness and remove points
            allGenome[i].fitness = i.toFloat()
        }
    }

    private fun calculateGlobalAdjustedFitness(): Float {
        return species.sumByDouble { it.totalAdjustedFitness.toDouble() }.toFloat()
    }

    private fun removeWeakGenomesFromSpecies() {
        species.forEach {
            it.removeWeakGenomes(false)
        }
    }

    private fun removeStaleSpecies() {
        val survived = ArrayList<Species>()

        if (topFitness < getTopFitness()) {
            poolStaleness = 0
        }

        for (s in species) {
            val top = s.topGenome
            if (top.fitness > s.topFitness) {
                s.topFitness = top.fitness
                s.staleness = 0
            } else {
                s.staleness = s.staleness + 1     // increment staleness
            }

            if (s.staleness < Config.STALE_SPECIES || s.topFitness >= this.getTopFitness()) {
                survived.add(s)
            }
        }

        Collections.sort(survived, Collections.reverseOrder())

        if (poolStaleness > Config.STALE_POOL) {
            for (i in survived.size downTo 2)
                survived.removeAt(i)
        }

        species = survived
        poolStaleness++
    }

    private fun calculateGenomeAdjustedFitness() {
        species.forEach {
            it.calculateGenomeAdjustedFitness()
        }
    }

    fun breedNewGeneration(): ArrayList<Genome> {
        calculateGenomeAdjustedFitness()
        val survived = ArrayList<Species>()

        removeWeakGenomesFromSpecies()
        removeStaleSpecies()
        val globalAdjustedFitness = calculateGlobalAdjustedFitness()
        val children = ArrayList<Genome>()
        var carryOver = 0f
        for (s in species) {
            val fchild =
                Config.POPULATION * (s.totalAdjustedFitness / globalAdjustedFitness)//- 1;       // reconsider
            var nchild = fchild.toInt()
            carryOver += fchild - nchild
            if (carryOver > 1) {
                nchild++
                carryOver -= 1f
            }

            if (nchild < 1)
                continue

            survived.add(Species(s.topGenome))
            //s.removeWeakGenome(nchild);

            //children.add(s.getTopGenome());
            for (i in 1 until nchild) {
                val child = s.breedChild()
                children.add(child)
            }


        }
        species = survived
        for (child in children)
            addToSpecies(child)
        //clearInnovations();
        generations++
        return children
    }

    private fun getTopFitness() = topGenome.fitness

}
