@file:JvmName("Instinct")
@file:JvmMultifileClass

package eu.timerertim.knevo.instinct

import eu.timerertim.knevo.Environment
import eu.timerertim.knevo.Population
import eu.timerertim.knevo.instinct.InstinctNetwork.Companion.offspring
import eu.timerertim.knevo.selection.FitnessProportionate
import eu.timerertim.knevo.selection.Power
import eu.timerertim.knevo.selection.SelectionFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.valueParameters

typealias Pool = InstinctPool
typealias PoolBuilder = InstinctPool.Builder
typealias InstinctPoolBuilder = PoolBuilder

/**
 * This class is used for building an [InstinctPool].
 */
@JvmSynthetic
fun InstinctInstance.PoolBuilder() = PoolBuilder(this)

/**
 * A pretty generic, simple [Population] implementation used with the algorithm described in [InstinctInstance]. It does
 * not feature speciation, but multithreading, different [SelectionFunction]s and size penalties for the
 * [InstinctNetwork]s. The [instance][InstinctPool.instance] is used for all operations requiring an InstinctInstance.
 *
 * Configurable Parameters:
 * - [populationSize]: The amount of Networks in this Population
 * - [batchSize]: The amount of Networks in a single batch. Batches are evaluated and breded in parallel.
 * - [elitism]: This many best performing networks will be in the next generation without any changes
 * - [crossoverChance]: The chance of performing a crossover when breeding the next generation (otherwise mutate)
 * - [nodesGrowth]: The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per hidden neuron
 * - [connectionsGrowth]: The value subtracted from the fitness of a network per connection
 * - [gatesGrowth]: The value subtracted from the fitness of a network per gated connection
 * - [select]: The SelectionFunction used by default when [breedNewGeneration][InstinctPool.breedNewGeneration] is
 * invoked
 */
@JvmSynthetic
fun InstinctInstance.Pool(
    populationSize: Int? = null,
    batchSize: Int? = null,
    elitism: Int? = null,
    crossoverChance: Float? = null,
    nodesGrowth: Float? = null,
    connectionsGrowth: Float? = null,
    gatesGrowth: Float? = null,
    select: SelectionFunction? = null
) = PoolBuilder().apply {
    populationSize(populationSize)
    batchSize(batchSize)
    elitism(elitism)
    crossoverChance(crossoverChance)
    nodesGrowth(nodesGrowth)
    connectionsGrowth(connectionsGrowth)
    gatesGrowth(gatesGrowth)
    select(select)
}.build()

/**
 * A pretty generic, simple [Population] implementation used with the algorithm described in [InstinctInstance]. It does
 * not feature speciation, but multithreading, different [SelectionFunction]s and size penalties for the
 * [InstinctNetwork]s. The [instance][InstinctPool.instance] is used for all operations requiring an InstinctInstance.
 *
 * Configurable Parameters:
 * - [populationSize][InstinctPool.size]: The amount of Networks in this Population
 * - [batchSize]: The amount of Networks in a single batch. Batches are evaluated and breded in parallel.
 * - [elitism]: This many best performing networks will be in the next generation without any changes
 * - [crossoverChance]: The chance of performing a crossover when breeding the next generation (otherwise mutate)
 * - [nodesGrowth]: The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per hidden neuron
 * - [connectionsGrowth]: The value subtracted from the fitness of a network per connection
 * - [gatesGrowth]: The value subtracted from the fitness of a network per gated connection
 * - [select]: The SelectionFunction used by default when [breedNewGeneration][InstinctPool.breedNewGeneration] is
 * invoked
 */
open class InstinctPool private constructor(
    val batchSize: Int,
    val elitism: Int,
    val crossoverChance: Float,
    val nodesGrowth: Float,
    val connectionsGrowth: Float,
    val gatesGrowth: Float,
    val select: SelectionFunction,
    val instance: InstinctInstance,
    protected val pool: MutableList<InstinctNetwork>
) : Population<InstinctNetwork>, List<InstinctNetwork> by pool {

    /**
     * A pretty generic, simple [Population] implementation used with the algorithm described in [InstinctInstance]. It
     * does not feature speciation, but multithreading, different [SelectionFunction]s and size penalties for the
     * [InstinctNetwork]s. The [instance][InstinctPool.instance] is used for all operations requiring an
     * InstinctInstance.
     *
     * Configurable Parameters:
     * - [populationSize]: The amount of Networks in this Population
     * - [batchSize]: The amount of Networks in a single batch. Batches are evaluated and breded in parallel.
     * - [elitism]: This many best performing networks will be in the next generation without any changes
     * - [crossoverChance]: The chance of performing a crossover when breeding the next generation (otherwise mutate)
     * - [nodesGrowth]: The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per hidden neuron
     * - [connectionsGrowth]: The value subtracted from the fitness of a network per connection
     * - [gatesGrowth]: The value subtracted from the fitness of a network per gated connection
     * - [select]: The SelectionFunction used by default when [breedNewGeneration][InstinctPool.breedNewGeneration] is
     * invoked
     */
    @JvmOverloads
    constructor(
        populationSize: Int = 400,
        batchSize: Int = populationSize,
        elitism: Int = 5,
        crossoverChance: Float = 0.75F,
        nodesGrowth: Float = 0F,
        connectionsGrowth: Float = 0F,
        gatesGrowth: Float = 0F,
        select: SelectionFunction = Power(),
        instance: InstinctInstance = globalInstinctInstance
    ) : this(
        batchSize,
        elitism,
        crossoverChance,
        nodesGrowth,
        connectionsGrowth,
        gatesGrowth,
        select,
        instance,
        Array(populationSize) { instance.Network() }.toMutableList()
    )

    /**
     * Keeps track of the current [generation] in this [Pool]. Is increased automatically upon invoking
     * [breedNewGeneration].
     */
    override var generation = 0L
        private set

    /**
     * Evolves this [InstinctPool] using a given [environment] and [select] method. It invokes [evaluateFitness] and
     * then [breedNewGeneration].
     */
    fun evolve(environment: Environment<InstinctNetwork>, select: SelectionFunction) {
        evaluateFitness(environment)
        breedNewGeneration(select)
    }

    override open fun evaluateFitness(environment: Environment<InstinctNetwork>) {
        runBlocking(Dispatchers.Default) {
            pool.chunked(batchSize)
                .map { batch ->
                    launch {
                        environment.evaluateFitness(batch)
                        batch.forEach {
                            it.score -= (it.nodes.size - instance.inputs - instance.outputs) * nodesGrowth +
                                    it.connections.size * connectionsGrowth +
                                    it.connections.count { con -> con.gater != null } * gatesGrowth
                        }
                    }
                }.joinAll()
        }
    }

    /**
     * Breeds a new generation based on the [evaluated Fitness][evaluateFitness]. It uses
     * the configured [select] method when selecting [InstinctNetwork]s during breeding. It increases [generation].
     */
    override open fun breedNewGeneration() = breedNewGeneration(select)

    /**
     * Breeds a new generation based on the [evaluated Fitness][evaluateFitness]. It uses
     * the given [select] method when selecting [InstinctNetwork]s during breeding. It increases [generation].
     */
    open fun breedNewGeneration(select: SelectionFunction) {
        pool.sortDescending()
        if (select is FitnessProportionate) select.reset()

        val newPopulation = runBlocking(Dispatchers.Default) {
            pool.chunked(batchSize)
                .map { batch ->
                    async {
                        val size = batch.size
                        Array(size) {
                            produceNextMember()
                        }.asList()
                    }
                }.flatMap { it.await() }
        }

        for (index in elitism until size) {
            pool[index] = newPopulation[index]
        }

        generation++
    }

    private fun produceNextMember(): InstinctNetwork {
        return if (Random.nextFloat() < crossoverChance) {
            // Offspring
            val parent1 = select(this)
            val parent2 = select(filter { it != parent1 })

            offspring(parent1, parent2)
        } else {
            // Mutated Member
            Network(select(this)).apply {
                mutate()
            }
        }
    }

    companion object {
        private const val serialVersionUID = 0L
    }

    /**
     * This class is used for building an [InstinctPool]. It requires an [instance] which defaults to
     * [globalInstinctInstance].
     */
    data class Builder @JvmOverloads constructor(
        val instance: InstinctInstance = globalInstinctInstance
    ) {
        /**
         * The amount of [InstinctNetwork]s in this [Pool].
         */
        var populationSize: Int? = null

        /**
         * The amount of [InstinctNetwork]s in a single batch. Batches are evaluated and breded in parallel, therefore
         * the used [Environment] during [evaluation][evaluateFitness] needs to be thread safe.
         */
        var batchSize: Int? = null

        /**
         * This many best performing networks will be in the next generation without any changes.
         */
        var elitism: Int? = null

        /**
         * The chance of performing a crossover when breeding the next generation. Otherwise, a mutation on an existing
         * [InstinctNetwork] is performed.
         */
        var crossoverChance: Float? = null

        /**
         * The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per hidden neuron. This
         * is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         */
        var nodesGrowth: Float? = null

        /**
         * The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per connection. This
         * is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         */
        var connectionsGrowth: Float? = null

        /**
         * The value subtracted from the [fitness][InstinctNetwork.fitness] of a network per gated connection. This
         * is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         */
        var gatesGrowth: Float? = null

        /**
         * The [SelectionFunction] used by default when [breedNewGeneration] is invoked.
         */
        var select: SelectionFunction? = null


        /**
         * Changes the amount of [InstinctNetwork]s in this [Pool].
         *
         * Returns this [Builder] to allow chaining.
         */
        fun populationSize(value: Int?) = apply { populationSize = value }

        /**
         * Changes the amount of Networks in a single batch. Batches are evaluated and breded in parallel.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun batchSize(value: Int?) = apply { batchSize = value }

        /**
         * Changes how many best performing networks will be in the next generation without any changes.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun elitism(value: Int?) = apply { elitism = value }

        /**
         * Changes the chance of performing a crossover when breeding the next generation.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun crossoverChance(value: Float?) = apply { crossoverChance = value }

        /**
         * Changes the value subtracted from the [fitness][InstinctNetwork.fitness] of a network per hidden neuron. This
         * is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun nodesGrowth(value: Float?) = apply { nodesGrowth = value }

        /**
         * Changes the value subtracted from the [fitness][InstinctNetwork.fitness] of a network per connection. This
         * is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun connectionsGrowth(value: Float?) = apply { connectionsGrowth = value }

        /**
         * Changes the value subtracted from the [fitness][InstinctNetwork.fitness] of a network per gated connection.
         * This is a penalty which affects the final [score][InstinctNetwork.score] of each network.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun gatesGrowth(value: Float?) = apply { gatesGrowth = value }

        /**
         * Changes the [SelectionFunction] used by default when [breedNewGeneration] is invoked.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun select(value: SelectionFunction?) = apply { select = value }

        /**
         * Sets the [growth][value] for [nodesGrowth], [connectionsGrowth] and [gatesGrowth] at once.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun growth(value: Float?) = apply {
            nodesGrowth = value
            connectionsGrowth = value
            gatesGrowth = value
        }


        /**
         * Builds an [InstinctPool] based on the properties of this [Builder]. Unspecified/null properties will
         * have the default values defined in the InstinctPool constructor.
         */
        fun build(): InstinctPool {
            val constructor = InstinctPool::class.constructors.first()
            val values = InstinctPoolBuilder::class.declaredMemberProperties
            val valueMap = constructor.valueParameters.associateWith { parameter ->
                values.firstOrNull { it.name == parameter.name }?.get(this)
            }.filter {
                it.value != null
            }
            return constructor.callBy(valueMap)
        }
    }
}
