package eu.timerertim.knevo.shared.multiplexer

import eu.timerertim.knevo.Environment
import eu.timerertim.knevo.Genome

class MultiplexerEnvironment(private val multiplexer: Multiplexer) : Environment<Genome> {
    override suspend fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            var fitness = 0f

            for (input in multiplexer.allPossibleInputs) {
                val prediction = gene(Multiplexer.convertToFloat(input)).first()
                fitness += multiplexer.fitness(input, prediction)
                gene.doReset = true
            }

            gene.fitness = fitness * 100 / multiplexer.allPossibleInputs.size
        }
    }
}
