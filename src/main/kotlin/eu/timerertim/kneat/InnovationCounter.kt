package eu.timerertim.kneat

object InnovationCounter {

    var innovation = 0

    fun newInnovation() = ++innovation

    fun reset() {
        innovation = 0
    }

}
