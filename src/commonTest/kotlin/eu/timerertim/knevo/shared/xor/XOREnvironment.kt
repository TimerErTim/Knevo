package eu.timerertim.knevo.shared.xor

import eu.timerertim.knevo.FitnessFunction
import eu.timerertim.knevo.Genome

class XOREnvironment : FitnessFunction<Genome> {
    override suspend fun invoke(genome: Genome): Float {
        var fitness = 0F
        for (i in 0..1)
            for (j in 0..1) {
                genome.doReset = true
                val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                val output = genome(inputs)
                val expected = i xor j
                fitness += (expected - output[0]) * (expected - output[0])
            }
        return 1 - fitness
    }
}
