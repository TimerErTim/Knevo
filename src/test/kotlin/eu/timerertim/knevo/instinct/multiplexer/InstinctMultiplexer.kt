package eu.timerertim.knevo.instinct.multiplexer

import eu.timerertim.knevo.activation.Linear
import eu.timerertim.knevo.activation.Relu
import eu.timerertim.knevo.activation.Selu
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sign
import eu.timerertim.knevo.activation.Silu
import eu.timerertim.knevo.activation.Sinus
import eu.timerertim.knevo.activation.Step
import eu.timerertim.knevo.activation.Tanh
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctNetwork
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.instinct.globalInstinctInstance
import eu.timerertim.knevo.selection.Tournament
import eu.timerertim.knevo.shared.multiplexer.Multiplexer
import eu.timerertim.knevo.shared.multiplexer.MultiplexerEnvironment
import java.io.File


fun main() {
    val network = runWith(3)
    network.save("out${File.separator}InstinctMultiplexer_fully_trained.knv")
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
