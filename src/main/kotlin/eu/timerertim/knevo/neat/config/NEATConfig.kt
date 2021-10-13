package eu.timerertim.knevo.neat.config

data class NEATConfig(
    val inputs: Int = NEATDefaults.INPUTS,
    val outputs: Int = NEATDefaults.OUTPUTS,
    val population: Int = NEATDefaults.POPULATION,
    val batchSize: Int = NEATDefaults.BATCH_SIZE
) {

    class Builder {

        private var inputs: Int = NEATDefaults.INPUTS
        private var outputs: Int = NEATDefaults.OUTPUTS
        private var population: Int = NEATDefaults.POPULATION
        private var batchSize: Int = NEATDefaults.BATCH_SIZE

        fun setInputs(inputs: Int): Builder {
            this.inputs = inputs
            return this
        }

        fun setOutputs(outputs: Int): Builder {
            this.outputs = outputs
            return this
        }

        fun setPopulationSize(population: Int): Builder {
            this.population = population
            return this
        }

        fun setBatchSize(batchSize: Int): Builder {
            this.batchSize = batchSize
            return this
        }

        fun build(): NEATConfig {
            return NEATConfig(inputs, outputs, population, batchSize)
        }
    }
}