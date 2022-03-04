package eu.timerertim.knevo.instinct.cppn

import eu.timerertim.knevo.activation.Modulo
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sinus
import eu.timerertim.knevo.activation.Tanh
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.instinct.globalInstinctInstance
import eu.timerertim.knevo.shared.cppn.CPPN
import eu.timerertim.knevo.shared.cppn.display
import eu.timerertim.knevo.shared.xor.XOREnvironment
import kotlinx.coroutines.delay

suspend fun main() {
    val environment = XOREnvironment()

    globalInstinctInstance = InstinctInstanceBuilder(2, 1)
        .mutateAddSelfConnectionChance(0F)
        .mutateAddRecurrentConnectionChance(0F)
        .mutateRemoveConnectionChance(2.15F)
        .hiddenActivations(listOf(Tanh(), Sinus(), Modulo()))
        .outputActivations(Sigmoid())
        .build()

    val pool = InstinctPoolBuilder()
        .populationSize(400)
        .batchSize(40)
        .growth(0.05F)
        .build()

    var cppn: CPPN
    do {
        pool.evolve(environment)
        cppn = CPPN(pool.topGenome)

        println("Top Fitness : " + pool.topFitness)
        println("Generation : ${pool.generation}")
        display(cppn.generateGray(scale = 10F))
        delay(25)
    } while (pool.topFitness < 1)

    val network = pool.topGenome

    println("------------")
    println("0 | 0 = ${network(floatArrayOf(0F, 0F)).first()}")
    println("0 | 1 = ${network(floatArrayOf(0F, 1F)).first()}")
    println("1 | 0 = ${network(floatArrayOf(1F, 0F)).first()}")
    println("1 | 1 = ${network(floatArrayOf(1F, 1F)).first()}")
    println("------------")
}
