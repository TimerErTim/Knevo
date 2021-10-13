package eu.timerertim.knevo.neat

import eu.timerertim.knevo.neat.config.NEATDefaults

typealias NEATInstanceBuilder = NEATInstance.Builder

var globalNeatInstance = NEATInstance(NEATDefaults.INPUTS, NEATDefaults.OUTPUTS)

data class NEATInstance @JvmOverloads constructor(
    val inputs: Int,
    val outputs: Int,

    val steps
) {
    data class Builder(val inputs: Int = NEATDefaults.INPUTS)
}
