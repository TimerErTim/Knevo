@file:JvmName("NEAT")
@file:JvmMultifileClass

package eu.timerertim.knevo.neat

typealias Node = NEATNode

/**
 * NodeGene represents the nodes of the neural network
 */
class NEATNode(var value: Float) {

    var isActivated: Boolean = false
    val connections = ArrayList<ConnectionGene>()

}
