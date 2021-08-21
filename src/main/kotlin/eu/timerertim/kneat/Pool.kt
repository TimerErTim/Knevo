package eu.timerertim.kneat

import eu.timerertim.kneat.config.Defaults
import eu.timerertim.kneat.config.NEATConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class Pool(private val config: NEATConfig = NEATConfig()) {

    private var species = ArrayList<Species>()

    private var generations = 0
    private val topFitness: Float = 0.toFloat()
    private var poolStaleness = 0

    private val random = Random(2000)

    val topGenome: Genome
        get() = species.flatMap { it.genomes }.maxByOrNull { it.fitness }!!


    fun initializePool() {
        for (i in 0 until config.population) {
            addToSpecies(Genome(config))
        }
    }

    private fun addToSpecies(genome: Genome) {
        for (species in species) {
            if (species.genomes.isEmpty()) {
                continue
            }

            val first = species.genomes.first()
            if (Genome.isSameSpecies(genome, first)) {
                species.genomes.add(genome)
                return
            }
        }

        val child = Species()
        child.genomes.add(genome)
        species.add(child)
    }

    fun evaluateFitness(environment: Environment) {
        runBlocking {
            species.flatMap { it.genomes }
                .chunked(config.batchSize)
                .map {
                    launch(context = Dispatchers.Default) {
                        environment.evaluateFitness(it)
                    }
                }.joinAll()
        }

        rankGlobally()
    }

    private fun rankGlobally() {
        species.flatMap { it.genomes }
            .sorted()
            .forEachIndexed { index, genome ->
                //TODO use adjustedFitness and remove points
                genome.points = genome.fitness
                genome.fitness = index.toFloat()
            }
    }

    private fun calculateGlobalAdjustedFitness(): Float {
        return species.sumOf { it.totalAdjustedFitness.toDouble() }.toFloat()
    }

    private fun removeWeakGenomesFromSpecies() {
        species.forEach {
            it.removeWeakGenomes()
        }
    }

    private fun removeStaleSpecies() {
        val survived = ArrayList<Species>()

        if (topFitness < getTopFitness()) {
            poolStaleness = 0
        }

        for (species in species) {
            val top = species.topGenome
            if (top.fitness > species.topFitness) {
                species.topFitness = top.fitness
                species.resetStaleness()
            } else {
                species.increaseStaleness()
            }

            if (species.isStale || species.topFitness >= getTopFitness()) {
                survived.add(species)
            }
        }

        survived.sortDescending()

        if (poolStaleness > Defaults.STALE_POOL) {
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

    fun breedNewGeneration() {
        calculateGenomeAdjustedFitness()
        val survived = ArrayList<Species>()

        removeWeakGenomesFromSpecies()
        removeStaleSpecies()
        val globalAdjustedFitness = calculateGlobalAdjustedFitness()
        val children = ArrayList<Genome>()
        var carryOver = 0f
        for (species in species) {
            val fchild = config.population * (species.totalAdjustedFitness / globalAdjustedFitness)
            var nchild = fchild.toInt()
            carryOver += fchild - nchild
            if (carryOver > 1) {
                nchild++
                carryOver -= 1f
            }

            if (nchild < 1)
                continue

            survived.add(Species(species.topGenome))

            for (i in 1 until nchild) {
                val child = species.breedChild()
                children.add(child)
            }
        }

        species.clear()
        species.addAll(survived)

        for (child in children) {
            addToSpecies(child)
        }

        generations++
    }

    private fun getTopFitness() = topGenome.fitness

}
