package examples

import eu.timerertim.kneat.Environment
import eu.timerertim.kneat.Genome
import eu.timerertim.kneat.Pool
import eu.timerertim.kneat.config.NEATConfig
import kotlin.math.abs

fun main() {
    val xor = XOR()

    val config = NEATConfig.Builder()
        .setPopulationSize(300)
        .setBatchSize(100)
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
    } while (topGenome.points < 15)

    val gene = pool.topGenome
    for (i in 0..1)
        for (j in 0..1) {
            print("$i $j - ")
            println(gene.evaluate(floatArrayOf(i.toFloat(), j.toFloat()))[0])
        }

    var input = floatArrayOf(1F, 0F)
    var result = gene.evaluate(input)[0]
    println(result)
    input = floatArrayOf(1F, 1F)
    result = gene.evaluate(input)[0]
    println(result)
    input = floatArrayOf(0F, 1F)
    result = gene.evaluate(input)[0]
    println(result)
}

class XOR : Environment {

    override fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            var fitness = 0f
            gene.fitness = 0f
            for (i in 0..1)
                for (j in 0..1) {
                    val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                    val output = gene.evaluate(inputs)
                    val expected = i xor j
                    fitness += 1 - abs(expected - output[0])
                }
            fitness *= fitness

            gene.fitness = fitness
        }
    }
}