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
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.ObjectInputStream
import kotlin.math.min
import kotlin.random.Random
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmName

typealias Pool = InstinctPool
typealias PoolBuilder = InstinctPool.Builder
typealias InstinctPoolBuilder = PoolBuilder

@JvmSynthetic
fun InstinctInstance.PoolBuilder() = PoolBuilder(this)

@JvmSynthetic
fun InstinctInstance.Pool(
    populationSize: Int? = null,
    threads: Int? = null,
    elitism: Int? = null,
    crossoverChance: Float? = null,
    nodesGrowth: Float? = null,
    connectionsGrowth: Float? = null,
    gatesGrowth: Float? = null,
    select: SelectionFunction? = null
) = PoolBuilder().apply {
    populationSize(populationSize)
    threads(threads)
    elitism(elitism)
    crossoverChance(crossoverChance)
    nodesGrowth(nodesGrowth)
    connectionsGrowth(connectionsGrowth)
    gatesGrowth(gatesGrowth)
    select(select)
}.build()


class InstinctPool @JvmOverloads constructor(
    val populationSize: Int = 400,
    val threads: Int = 1,
    val elitism: Int = 5,
    val crossoverChance: Float = 0.75F,
    val nodesGrowth: Float = 0F,
    val connectionsGrowth: Float = 0F,
    val gatesGrowth: Float = 0F,
    val select: SelectionFunction = Power(exponent = 5),
    private val pool: MutableList<InstinctNetwork> = Array(populationSize) { instance.Network() }.toMutableList(),
    val instance: InstinctInstance = globalInstinctInstance
) : Population<InstinctNetwork>, List<InstinctNetwork> by pool {
    override var generation = 0L

    fun evolve(environment: Environment<InstinctNetwork>, select: SelectionFunction) {
        breedNewGeneration(select)
        evaluateFitness(environment)
    }

    override fun evaluateFitness(environment: Environment<InstinctNetwork>) {
        runBlocking(Dispatchers.Default) {
            pool.chunked(populationSize / threads + min(populationSize % threads, 1))
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

    override fun breedNewGeneration() = breedNewGeneration(select)

    fun breedNewGeneration(select: SelectionFunction) {
        pool.sortDescending()
        if (select is FitnessProportionate) select.reset()

        val newPopulation = runBlocking(Dispatchers.Default) {
            pool.chunked(populationSize / threads + min(populationSize % threads, 1))
                .map { batch ->
                    async {
                        val size = batch.size
                        Array(size) {
                            produceNextMember()
                        }.asList()
                    }
                }.flatMap { it.await() }
        }

        for (index in elitism until populationSize) {
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

        @JvmStatic
        fun load(input: InputStream): InstinctPool {
            val population = ObjectInputStream(input).readObject()
            check(population is InstinctPool) {
                "Cannot load object of type \"${population::class.jvmName}\" as InstinctPool"
            }
            return population
        }

        @JvmStatic
        @Suppress("SwallowedException")
        fun load(path: String): InstinctPool? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }

    data class Builder @JvmOverloads constructor(
        val instance: InstinctInstance = globalInstinctInstance
    ) {
        var populationSize: Int? = null
        var threads: Int? = null
        var elitism: Int? = null
        var crossoverChance: Float? = null
        var nodesGrowth: Float? = null
        var connectionsGrowth: Float? = null
        var gatesGrowth: Float? = null
        var select: SelectionFunction? = null

        fun populationSize(value: Int?) = apply { populationSize = value }
        fun threads(value: Int?) = apply { threads = value }
        fun elitism(value: Int?) = apply { elitism = value }
        fun crossoverChance(value: Float?) = apply { crossoverChance = value }
        fun nodesGrowth(value: Float?) = apply { nodesGrowth = value }
        fun connectionsGrowth(value: Float?) = apply { connectionsGrowth = value }
        fun gatesGrowth(value: Float?) = apply { gatesGrowth = value }
        fun select(value: SelectionFunction?) = apply { select = value }

        fun parallelization(value: Boolean) =
            apply { threads = if (value) Runtime.getRuntime().availableProcessors() else 1 }

        fun growth(value: Float?) = apply {
            nodesGrowth = value
            connectionsGrowth = value
            gatesGrowth = value
        }

        fun build(): InstinctPool {
            val constructor = InstinctPool::class.primaryConstructor!!
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
