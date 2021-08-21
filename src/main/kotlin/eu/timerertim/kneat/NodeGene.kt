package eu.timerertim.kneat

/**
 * NodeGene represents the nodes of the neural network
 */
class NodeGene(var value: Float) {

    var isActivated: Boolean = false
    val connections = ArrayList<ConnectionGene>()

}
