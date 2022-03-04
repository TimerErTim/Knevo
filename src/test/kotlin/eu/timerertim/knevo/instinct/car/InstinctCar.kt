package eu.timerertim.knevo.instinct.car

import eu.timerertim.knevo.activation.Relu
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sinus
import eu.timerertim.knevo.instinct.InstinctInstanceBuilder
import eu.timerertim.knevo.instinct.InstinctPoolBuilder
import eu.timerertim.knevo.instinct.globalInstinctInstance
import eu.timerertim.knevo.selection.Power

fun main() {
    globalInstinctInstance = InstinctInstanceBuilder(18, 3).apply {
        mutateAddSelfConnectionChance(5.40F)
        mutateAddRecurrentConnectionChance(4.30F)
        mutateRemoveConnectionChance(8.15F)
        mutateWeightChance(10.50F)
        hiddenActivations(Sigmoid(), Sinus(), Relu())
        outputActivations(Sigmoid(slope = 10.0F))
    }.build()

    val pool = InstinctPoolBuilder()
        .populationSize(400)
        .batchSize(40)
        .growth(0.005F)
        .select(Power())
        .build()

    eu.timerertim.knevo.shared.car.run(pool, 50, "Instinct Car Training")
}
