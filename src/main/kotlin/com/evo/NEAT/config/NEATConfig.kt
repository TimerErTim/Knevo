package com.evo.NEAT.config

data class NEATConfig(
    val inputs: Int = Defaults.INPUTS,
    val outputs: Int = Defaults.OUTPUTS,
    val hiddenNodes: Int = Defaults.HIDDEN_NODES,
    val population: Int = Defaults.POPULATION,
    val batchSize: Int = Defaults.BATCH_SIZE
) {

    class Builder {

        private var inputs: Int = Defaults.INPUTS
        private var outputs: Int = Defaults.OUTPUTS
        private var hiddenNodes: Int = Defaults.HIDDEN_NODES
        private var population: Int = Defaults.POPULATION
        private var batchSize: Int = Defaults.BATCH_SIZE

        fun setInputs(inputs: Int): Builder {
            this.inputs = inputs
            return this
        }

        fun setOutputs(outputs: Int): Builder {
            this.outputs = outputs
            return this
        }

        fun setHiddenNodes(hiddenNodes: Int): Builder {
            this.hiddenNodes = hiddenNodes
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
            return NEATConfig(inputs, outputs, hiddenNodes, population, batchSize)
        }
    }
}