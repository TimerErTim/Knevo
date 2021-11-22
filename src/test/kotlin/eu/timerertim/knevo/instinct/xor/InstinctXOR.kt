package eu.timerertim.knevo.instinct.xor

import eu.timerertim.knevo.activation.Relu
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sign
import eu.timerertim.knevo.activation.Step
import eu.timerertim.knevo.activation.Tanh
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.instinct.globalInstinctInstance
import eu.timerertim.knevo.serialization.save
import eu.timerertim.knevo.shared.xor.XOREnvironment
import java.io.File

fun main() {
    val environment = XOREnvironment()

    globalInstinctInstance = InstinctInstanceBuilder(2, 1)
        .mutateAddSelfConnectionChance(0F)
        .mutateAddRecurrentConnectionChance(0F)
        .mutateRemoveConnectionChance(2.15F)
        .hiddenActivations(listOf(Tanh(), Sigmoid(), Relu(), Sign()))
        .outputActivations(Step())
        .build()

    val pool = InstinctPoolBuilder()
        .populationSize(400)
        .batchSize(40)
        .growth(0.05F)
        .build()

    do {
        pool.evolve(environment)

        println("Top Fitness : " + pool.topFitness)
        println("Generation : ${pool.generation}")

    } while (pool.topFitness < 1)

    val network = pool.topGenome

    println("------------")
    println("0 | 0 = ${network(floatArrayOf(0F, 0F)).first()}")
    println("0 | 1 = ${network(floatArrayOf(0F, 1F)).first()}")
    println("1 | 0 = ${network(floatArrayOf(1F, 0F)).first()}")
    println("1 | 1 = ${network(floatArrayOf(1F, 1F)).first()}")
    println("------------")

    pool.topGenome.save("out${File.separator}instinctXOR_fully_trained.knv")
}
