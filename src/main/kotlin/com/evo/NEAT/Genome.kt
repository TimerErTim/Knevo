package com.evo.NEAT

import com.evo.NEAT.config.NEATConfig
import com.evo.NEAT.config.Defaults
import com.evo.NEAT.config.Seed

import javax.management.RuntimeErrorException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Genome(private val config: NEATConfig) : Comparable<Genome>, Cloneable {
    // Global Percentile Rank (higher the better)
    var fitness: Float = 0.toFloat()
    var points: Float = 0.toFloat()

    // DNA- MAin archive of gene information
    var connectionGeneList = ArrayList<ConnectionGene>()

    // Generated while performing network operation
    private val nodes =
        TreeMap<Int, NodeGene>()

    // For number of child to breed in species
    var adjustedFitness: Float = 0f

    val mutationRates = HashMap<MutationKeys, Float>()

    enum class MutationKeys {
        STEPS,
        PERTURB_CHANCE,
        WEIGHT_CHANCE,
        WEIGHT_MUTATION_CHANCE,
        NODE_MUTATION_CHANCE,
        CONNECTION_MUTATION_CHANCE,
        BIAS_CONNECTION_MUTATION_CHANCE,
        DISABLE_MUTATION_CHANCE,
        ENABLE_MUTATION_CHANCE
    }

    init {
        mutationRates[MutationKeys.STEPS] = Defaults.STEPS
        mutationRates[MutationKeys.PERTURB_CHANCE] = Defaults.PERTURB_CHANCE
        mutationRates[MutationKeys.WEIGHT_CHANCE] = Defaults.WEIGHT_CHANCE
        mutationRates[MutationKeys.WEIGHT_MUTATION_CHANCE] = Defaults.WEIGHT_MUTATION_CHANCE
        mutationRates[MutationKeys.NODE_MUTATION_CHANCE] = Defaults.NODE_MUTATION_CHANCE
        mutationRates[MutationKeys.CONNECTION_MUTATION_CHANCE] = Defaults.CONNECTION_MUTATION_CHANCE
        mutationRates[MutationKeys.BIAS_CONNECTION_MUTATION_CHANCE] = Defaults.BIAS_CONNECTION_MUTATION_CHANCE
        mutationRates[MutationKeys.DISABLE_MUTATION_CHANCE] = Defaults.DISABLE_MUTATION_CHANCE
        mutationRates[MutationKeys.ENABLE_MUTATION_CHANCE] = Defaults.ENABLE_MUTATION_CHANCE
    }

    // todo: improve
    public override fun clone(): Genome {
        val genome = Genome(config)

        for (c in connectionGeneList) {
            genome.connectionGeneList.add(c.clone())
        }

        genome.fitness = fitness
        genome.adjustedFitness = adjustedFitness

        genome.mutationRates.clear()

        mutationRates.forEach { key, value ->
            genome.mutationRates[key] = value
        }

        return genome
    }

    private fun generateNetwork() {
        nodes.clear()
        //  Input layer
        for (i in 0 until config.inputs) {
            nodes[i] = NodeGene(0f)                    //Inputs
        }
        nodes[config.inputs] = NodeGene(1f)        // Bias

        //output layer
        for (i in config.inputs + config.hiddenNodes until config.inputs + config.hiddenNodes + config.outputs) {
            nodes[i] = NodeGene(0f)
        }

        // hidden layer
        for (con in connectionGeneList) {
            if (!nodes.containsKey(con.into))
                nodes[con.into] = NodeGene(0f)
            if (!nodes.containsKey(con.out))
                nodes[con.out] = NodeGene(0f)
            nodes[con.out]!!.connections.add(con)
        }
    }

    fun evaluateNetwork(inputs: FloatArray): FloatArray {
        val output = FloatArray(config.outputs)
        generateNetwork()

        for (i in 0 until config.inputs) {
            nodes[i]!!.value = inputs[i]
        }

        for ((key, node) in nodes) {
            var sum = 0f

            if (key > config.inputs) {
                for (conn in node.connections) {
                    if (conn.isEnabled) {
                        sum += nodes[conn.into]!!.value * conn.weight
                    }
                }
                node.value = sigmoid(sum)
            }
        }

        for (i in 0 until config.outputs) {
            output[i] = nodes[config.inputs + config.hiddenNodes + i]!!.value
        }
        return output
    }

    private fun sigmoid(x: Float): Float {
        return (1 / (1 + Math.exp(-4.9 * x))).toFloat()
    }

    // Mutations

    fun mutate() {
        // mutate mutation rates
        for ((key, value) in mutationRates) {
            if (Seed.random.nextBoolean()) {
                mutationRates[key] = 0.95f * value
            } else {
                mutationRates[key] = 1.05263f * value
            }
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.WEIGHT_MUTATION_CHANCE]!!) {
            mutateWeight()
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.CONNECTION_MUTATION_CHANCE]!!) {
            mutateAddConnection(false)
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.BIAS_CONNECTION_MUTATION_CHANCE]!!) {
            mutateAddConnection(true)
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.NODE_MUTATION_CHANCE]!!) {
            mutateAddNode()
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.DISABLE_MUTATION_CHANCE]!!) {
            disableMutate()
        }

        if (Seed.random.nextFloat() <= mutationRates[MutationKeys.ENABLE_MUTATION_CHANCE]!!) {
            enableMutate()
        }
    }

    private fun mutateWeight() {
        for (c in connectionGeneList) {
            if (Seed.random.nextFloat() < Defaults.WEIGHT_CHANCE) {
                if (Seed.random.nextFloat() < Defaults.PERTURB_CHANCE)
                    c.weight = c.weight + (2 * Seed.random.nextFloat() - 1) * Defaults.STEPS
                else
                    c.weight = 4 * Seed.random.nextFloat() - 2
            }
        }
    }

    private fun mutateAddConnection(forceBais: Boolean) {
        generateNetwork()
        var i = 0
        var j = 0
        val random2 = Seed.random.nextInt(nodes.size - config.inputs - 1) + config.inputs + 1
        var random1 = Seed.random.nextInt(nodes.size)
        if (forceBais)
            random1 = config.inputs
        var node1 = -1
        var node2 = -1

        for (k in nodes.keys) {
            if (random1 == i) {
                node1 = k
                break
            }
            i++
        }

        for (k in nodes.keys) {
            if (random2 == j) {
                node2 = k
                break
            }
            j++
        }

        if (node1 >= node2)
            return

        for (con in nodes[node2]!!.connections) {
            if (con.into == node1)
                return
        }

        if (node1 < 0 || node2 < 0)
            throw RuntimeErrorException(null)          // TODO Pool.newInnovation(node1, node2)
        connectionGeneList.add(
            ConnectionGene(
                node1,
                node2,
                InnovationCounter.newInnovation(),
                4 * Seed.random.nextFloat() - 2,
                true
            )
        )                // Add innovation and weight

    }

    internal fun mutateAddNode() {
        generateNetwork()
        if (connectionGeneList.size > 0) {
            var timeoutCount = 0
            var randomCon = connectionGeneList[Seed.random.nextInt(connectionGeneList.size)]
            while (!randomCon.isEnabled) {
                randomCon = connectionGeneList[Seed.random.nextInt(connectionGeneList.size)]
                timeoutCount++
                if (timeoutCount > config.hiddenNodes)
                    return
            }
            val nextNode = nodes.size - config.outputs
            randomCon.isEnabled = false
            connectionGeneList.add(
                ConnectionGene(
                    randomCon.into,
                    nextNode,
                    InnovationCounter.newInnovation(),
                    1f,
                    true
                )
            )        // Add innovation and weight
            connectionGeneList.add(
                ConnectionGene(
                    nextNode,
                    randomCon.out,
                    InnovationCounter.newInnovation(),
                    randomCon.weight,
                    true
                )
            )
        }
    }

    private fun disableMutate() {
        if (connectionGeneList.isNotEmpty()) {
            val gene = connectionGeneList.random(Seed.random)
            gene.isEnabled = false
        }
    }

    private fun enableMutate() {
        if (connectionGeneList.isNotEmpty()) {
            val gene = connectionGeneList.random(Seed.random)
            gene.isEnabled = true
        }
    }

    override operator fun compareTo(other: Genome): Int {
        return fitness.compareTo(other.fitness)
    }

    override fun toString(): String {
        return "Genome{" +
                "fitness=" + fitness +
                ", connectionGeneList=" + connectionGeneList +
                ", nodeGenes=" + nodes +
                '}'.toString()
    }

    fun adjustFitness(size: Int) {
        adjustedFitness = fitness / size
    }

    companion object {

        fun crossOver(parent1: Genome, parent2: Genome): Genome {
            var parent1 = parent1
            var parent2 = parent2
            if (parent1.fitness < parent2.fitness) {
                val temp = parent1
                parent1 = parent2
                parent2 = temp
            }

            val child = Genome(parent1.config)
            val geneMap1 = TreeMap<Int, ConnectionGene>()
            val geneMap2 = TreeMap<Int, ConnectionGene>()

            for (con in parent1.connectionGeneList) {
                geneMap1[con.innovation] = con
            }

            for (con in parent2.connectionGeneList) {
                geneMap2[con.innovation] = con
            }

            val innovationP1 = geneMap1.keys
            val innovationP2 = geneMap2.keys

            val allInnovations = HashSet(innovationP1)
            allInnovations.addAll(innovationP2)

            for (key in allInnovations) {
                val trait: ConnectionGene?

                if (geneMap1.containsKey(key) && geneMap2.containsKey(key)) {
                    trait = if (Seed.random.nextBoolean()) {
                        geneMap1[key]?.clone()
                    } else {
                        geneMap2[key]?.clone()
                    }

                    if (geneMap1[key]!!.isEnabled != geneMap2[key]!!.isEnabled) {
                        trait?.isEnabled = Seed.random.nextFloat() >= 0.75f
                    }

                } else if (parent1.fitness == parent2.fitness) {               // disjoint or excess and equal fitness
                    trait = if (geneMap1.containsKey(key))
                        geneMap1[key]
                    else
                        geneMap2[key]

                    if (Seed.random.nextBoolean()) {
                        continue
                    }

                } else
                    trait = geneMap1[key]


                if (trait != null) {
                    child.connectionGeneList.add(trait)
                }
            }

            // todo: should not need .clone()
            return child.clone()
        }


        fun isSameSpecies(g1: Genome, g2: Genome): Boolean {
            val geneMap1 = TreeMap<Int, ConnectionGene>()
            val geneMap2 = TreeMap<Int, ConnectionGene>()

            var matching = 0
            var disjoint = 0
            var excess = 0
            var weight = 0f
            val lowMaxInnovation: Int
            var delta = 0f

            for (con in g1.connectionGeneList) {
                geneMap1[con.innovation] = con
            }

            for (con in g2.connectionGeneList) {
                geneMap2[con.innovation] = con
            }
            if (geneMap1.isEmpty() || geneMap2.isEmpty())
                lowMaxInnovation = 0
            else
                lowMaxInnovation = Math.min(geneMap1.lastKey(), geneMap2.lastKey())

            val innovationP1 = geneMap1.keys
            val innovationP2 = geneMap2.keys

            val allInnovations = HashSet(innovationP1)
            allInnovations.addAll(innovationP2)

            for (key in allInnovations) {

                if (geneMap1.containsKey(key) && geneMap2.containsKey(key)) {
                    matching++
                    weight += Math.abs(geneMap1[key]!!.weight - geneMap2[key]!!.weight)
                } else {
                    if (key < lowMaxInnovation) {
                        disjoint++
                    } else {
                        excess++
                    }
                }

            }

            //System.out.println("matching : "+matching + "\ndisjoint : "+ disjoint + "\nExcess : "+ excess +"\nWeight : "+ weight);

            val N = matching + disjoint + excess

            if (N > 0)
                delta =
                    (Defaults.EXCESS_COEFFICENT * excess + Defaults.DISJOINT_COEFFICENT * disjoint) / N + Defaults.WEIGHT_COEFFICENT * weight / matching

            return delta < Defaults.COMPATIBILITY_THRESHOLD

        }
    }
}
