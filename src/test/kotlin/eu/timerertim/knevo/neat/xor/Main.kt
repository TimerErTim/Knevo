package eu.timerertim.knevo.neat.xor

import eu.timerertim.knevo.neat.Environment
import eu.timerertim.knevo.neat.Genome
import eu.timerertim.knevo.neat.Pool
import eu.timerertim.knevo.neat.config.NEATConfig
import kotlin.math.abs

fun main() {
    val xor = XOR()

    val config = NEATConfig.Builder()
        .setPopulationSize(300)
        .setBatchSize(75)
        .setInputs(2)
        .setOutputs(1)
        .build()

    val pool = Pool(config)
    pool.initializePool()

    var topGenome: Genome
    var generation = 0


    do {
        pool.evaluateFitness(xor)
        topGenome = pool.topGenome

        println("TopFitness : " + topGenome.points)
        println("Generation : $generation")

        pool.breedNewGeneration()
        generation++
    } while (topGenome.points < 15.9)

    val gene = pool.topGenome
    for (i in 1 downTo 0)
        for (j in 1 downTo 0) {
            print("$i $j - ")
            println(gene(floatArrayOf(i.toFloat(), j.toFloat()))[0])
        }
}

class XOR : Environment {

    override fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            var fitness = 0f
            gene.fitness = 0f
            for (i in 0..1)
                for (j in 0..1) {
                    val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                    val output = gene(inputs)
                    val expected = i xor j
                    fitness += 1 - abs(expected - output[0])
                }
            fitness *= fitness

            gene.fitness = fitness
        }
    }
}