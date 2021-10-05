package eu.timerertim.knevo.neat

/**
 * ConnectionGene represents the connection(Axon) of the neuron.
 */
data class ConnectionGene(
    val from: Int,
    val to: Int,
    var weight: Float,
    var isEnabled: Boolean
) : Cloneable {
    val innovation = from.toLong() shl 32 or to.toLong()

    public override fun clone(): ConnectionGene {
        return super.clone() as ConnectionGene
    }

    override fun toString() = "$from->$to:$weight=$isEnabled"
}
