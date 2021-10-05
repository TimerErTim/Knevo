package eu.timerertim.knevo.neat

import kotlin.random.Random

val globalNeat = NEATInstance()

class NEATInstance @JvmOverloads constructor(seed: Long? = null) {
    val random = if (seed == null) Random else Random(seed)
}