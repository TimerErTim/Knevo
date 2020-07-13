package com.evo.NEAT

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 */
class ConnectionGene {

    var into: Int = 0
        private set
    var out: Int = 0
        private set
    var innovation: Int = 0
        private set
    var weight: Float = 0.toFloat()
    var isEnabled: Boolean = false

    constructor(into: Int, out: Int, innovation: Int, weight: Float, enabled: Boolean) {
        this.into = into
        this.out = out
        this.innovation = innovation
        this.weight = weight
        this.isEnabled = enabled
    }

    // Copy
    constructor(connectionGene: ConnectionGene?) {
        if (connectionGene != null) {
            this.into = connectionGene.into
            this.out = connectionGene.out
            this.innovation = connectionGene.innovation
            this.weight = connectionGene.weight
            this.isEnabled = connectionGene.isEnabled
        }
    }


    override fun toString(): String {
        /*        return "ConnectionGene{" +
                "into=" + into +
                ", out=" + out +
                ", innovation=" + innovation +
                ", weight=" + weight +
                ", enabled=" + enabled +
                '}';*/
        return "$into,$out,$weight,$isEnabled"
    }
}
