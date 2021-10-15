package eu.timerertim.knevo.shared.xor

import eu.timerertim.knevo.Environment
import eu.timerertim.knevo.Genome

class XOREnvironment : Environment<Genome> {
    override fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            var fitness = 0f
            for (i in 0..1)
                for (j in 0..1) {
                    val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                    val output = gene(inputs)
                    gene.doReset = true
                    val expected = i xor j
                    fitness += (expected - output[0]) * (expected - output[0])
                }
            gene.fitness = 1 - fitness
        }
    }
}
