package eu.timerertim.knevo.instinct.xor

import eu.timerertim.knevo.activation.Relu
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sign
import eu.timerertim.knevo.activation.Tanh
import eu.timerertim.knevo.instinct.DefaultInstinctInstance
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.shared.xor.XOREnvironment

fun main() {
    val environment = XOREnvironment()

    DefaultInstinctInstance = InstinctInstanceBuilder(2, 1)
        .mutateAddSelfConnectionChance(0F)
        .mutateAddRecurrentConnectionChance(0F)
        .mutateRemoveConnectionChance(2.15F)
        .hiddenActivations(listOf(Tanh(), Sigmoid(), Relu(), Sign()))
        .build()

    val pool = InstinctPoolBuilder()
        .populationSize(400)
        .parallelization(true)
        .growth(0.05F)
        .build()

    do {
        pool.evolve(environment)

        println("Top Fitness : " + pool.topFitness)
        println("Generation : ${pool.generation}")

    } while (pool.topFitness < 3.95)
}