package com.evo.NEAT

import java.util.ArrayList

/**
 * NodeGene represents the nodes of the neural network
 */
class NodeGene(var value: Float) {

    val connections = ArrayList<ConnectionGene>()

}
