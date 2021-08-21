package eu.timerertim.kneat

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 */
data class ConnectionGene(
    val into: Int,
    val out: Int,
    val innovation: Int,
    var weight: Float,
    var isEnabled: Boolean
) : Cloneable {

    public override fun clone(): ConnectionGene {
        return super.clone() as ConnectionGene
    }

    override fun toString() = "$innovation:$into->$out=$weight|$isEnabled"
}
