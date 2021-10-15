package eu.timerertim.knevo.neat.multiplexer

import eu.timerertim.knevo.neat.Network
import eu.timerertim.knevo.neat.Pool
import eu.timerertim.knevo.neat.config.NEATConfig
import eu.timerertim.knevo.shared.multiplexer.Multiplexer
import eu.timerertim.knevo.shared.multiplexer.MultiplexerEnvironment

fun main() {
    runWith(2)
}


fun runWith(addressBits: Int) {
    val multiplexer = Multiplexer(addressBits)
    val environment = MultiplexerEnvironment(multiplexer)

    val config = NEATConfig.Builder()
        .setPopulationSize(200)
        .setBatchSize(50)
        .setInputs(multiplexer.totalBits)
        .setOutputs(1)
        .build()

    val pool = Pool(config).apply { initializePool() }

    var topGenome: Network
    var generation: Int = 0

    do {
        pool.evaluateFitness(environment)
        topGenome = pool.topGenome

        println("Top Fitness : " + topGenome.points)
        println("Generation : $generation")

        pool.breedNewGeneration()
        generation++
    } while (topGenome.points < 99)
}