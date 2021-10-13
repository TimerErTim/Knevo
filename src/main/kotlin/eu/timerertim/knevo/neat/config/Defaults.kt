package eu.timerertim.knevo.neat.config

typealias Defaults = NEATDefaults

object NEATDefaults {

    const val INPUTS = 2
    const val OUTPUTS = 1
    const val POPULATION = 300
    const val BATCH_SIZE = 100

    const val COMPATIBILITY_THRESHOLD = 1F
    const val DISJOINT_COEFFICENT = 2F
    const val WEIGHT_COEFFICENT = 0.4F

    const val STALE_SPECIES = 15F

    const val STEPS = 0.1F
    const val PERTURB_CHANCE = 0.9F
    const val WEIGHT_CHANCE = 0.3F
    const val WEIGHT_MUTATION_CHANCE = 0.9F
    const val NODE_MUTATION_CHANCE = 0.03F
    const val CONNECTION_MUTATION_CHANCE = 0.05F
    const val BIAS_CONNECTION_MUTATION_CHANCE = 0.15F
    const val DISABLE_MUTATION_CHANCE = 0.1F
    const val ENABLE_MUTATION_CHANCE = 0.2F
    const val CROSSOVER_CHANCE = 0.75F

    const val STALE_POOL = 20
}

