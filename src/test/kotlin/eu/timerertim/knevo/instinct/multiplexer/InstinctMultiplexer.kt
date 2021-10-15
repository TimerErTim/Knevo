package eu.timerertim.knevo.instinct.multiplexer

import eu.timerertim.knevo.activation.*
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctNetwork
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.instinct.globalInstinctInstance
import eu.timerertim.knevo.selection.Tournament
import eu.timerertim.knevo.shared.multiplexer.Multiplexer
import eu.timerertim.knevo.shared.multiplexer.MultiplexerEnvironment


fun main() {
    runWith(3)
}


fun runWith(addressBits: Int): InstinctNetwork {
    val multiplexer = Multiplexer(addressBits)
    val environment = MultiplexerEnvironment(multiplexer)

    globalInstinctInstance = InstinctInstanceBuilder(multiplexer.totalBits, 1).apply {
        mutateAddSelfConnectionChance(0F)
        mutateAddRecurrentConnectionChance(0F)
        mutateRemoveConnectionChance(2.15F)
        hiddenActivations(Sigmoid(), Tanh(), Step(), Sign(), Linear(), Sinus(), Relu(), Selu(), Silu())
        outputActivations(Sign())
    }.build()

    val pool = InstinctPoolBuilder()
        .populationSize(400)
        .parallelization(true)
        .growth(0.005F)
        .select(Tournament(10))
        .build()

    do {
        pool.evaluateFitness(environment)

        println("Top Fitness : " + pool.topFitness)
        println("Generation : ${pool.generation}")

        pool.breedNewGeneration()
    } while (pool.topFitness < 100)

    return pool.topGenome
}