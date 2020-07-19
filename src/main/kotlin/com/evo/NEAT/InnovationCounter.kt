package com.evo.NEAT

object InnovationCounter {

    var innovation = 0

    fun newInnovation() = ++innovation

    fun reset() {
        innovation = 0
    }

}
