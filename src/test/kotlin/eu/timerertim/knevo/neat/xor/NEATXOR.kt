package eu.timerertim.knevo.neat.xor

import eu.timerertim.knevo.neat.NEATNetwork
import eu.timerertim.knevo.neat.Pool
import eu.timerertim.knevo.neat.config.NEATConfig
import eu.timerertim.knevo.shared.xor.XOREnvironment

fun main() {
    val xor = XOREnvironment()

    val config = NEATConfig.Builder()
        .setPopulationSize(300)
        .setBatchSize(75)
        .setInputs(2)
        .setOutputs(1)
        .build()

    val pool = Pool(config)
    pool.initializePool()

    var topGenome: NEATNetwork
    var generation = 0


    do {
        pool.evaluateFitness(xor)
        topGenome = pool.topGenome

        println("TopFitness : " + topGenome.points)
        println("Generation : $generation")

        pool.breedNewGeneration()
        generation++
    } while (topGenome.points < 0.99)

    val gene = pool.topGenome
    for (i in 1 downTo 0)
        for (j in 1 downTo 0) {
            print("$i $j - ")
            println(gene(floatArrayOf(i.toFloat(), j.toFloat()))[0])
            gene.doReset = true
        }
}
