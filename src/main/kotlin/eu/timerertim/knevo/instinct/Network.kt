@file:JvmName("Instinct")
@file:JvmMultifileClass

package eu.timerertim.knevo.instinct

import eu.timerertim.knevo.Genome
import eu.timerertim.knevo.activation.ActivationFunction
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.OptionalDataException
import java.io.Serializable
import java.io.StreamCorruptedException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.reflect.jvm.jvmName

typealias Network = InstinctNetwork
typealias InstinctConnection = InstinctNetwork.Connection
typealias InstinctNode = InstinctNetwork.Node

/**
 * A [Genome] as described by the neuroevolution algorithm described in [InstinctInstance]. It consists of
 * [connections][InstinctNetwork.connections] and [nodes][InstinctNetwork.nodes]. The
 * [instance][InstinctNetwork.instance] used to create this network determines its mutation chances, possible
 * [ActivationFunction]s and [input][InstinctInstance.inputs] and [output][InstinctInstance.outputs] size.
 */
fun InstinctInstance.Network() = InstinctNetwork(this)

/**
 * A [Genome] as described by the neuroevolution algorithm described in [InstinctInstance]. It consists of [connections]
 * and [nodes]. The [instance] used to create this network determines its mutation chances, possible
 * [ActivationFunction]s and [input][InstinctInstance.inputs] and [output][InstinctInstance.outputs] size.
 */
class InstinctNetwork @JvmOverloads constructor(val instance: InstinctInstance = globalInstinctInstance) :
    Genome {
    /**
     * Create a new [InstinctNetwork] which is [base]d on the given one (creates basically a copy). The [instance] is
     * inferred from the given network.
     */
    constructor(base: InstinctNetwork) : this(base.instance) {
        nodes.clear()
        base.nodes.sortedBy {
            when (it) {
                is InputNode -> 0
                is OutputNode -> 1
                else -> 2
            }
        }.forEachIndexed { index, node ->
            when (node) {
                is InputNode -> InputNode(node.activate)
                is OutputNode -> OutputNode(node.bias, node.activate)
                else -> HiddenNode(index - instance.outputs, node.bias, node.activate)
            }
        }
        base.connections.forEach {
            Connection(it.fromIndex, it.toIndex, it.weight, it.gaterIndex)
        }
    }

    // Genes
    /**
     * A list of all [Connection]s in this Network. More or less the genotype together with [nodes].
     */
    val connections = object : ArrayList<InstinctConnection>() {
        override fun removeAt(index: Int): InstinctConnection {
            val element = get(index)
            remove(element)
            return element
        }

        override fun remove(element: InstinctConnection): Boolean {
            val result = super.remove(element)
            if (element.to != element.from) {
                element.to.incomingConnections -= element
                element.from.outgoingConnections -= element
            } else
                element.from.selfConnection = null
            return result
        }
    }

    /**
     * A list of all [Node]s in this Network. More or less the genotype together with [connections].
     */
    val nodes = object : ArrayList<InstinctNode>() {
        override fun clear() {
            super.clear()
            connections.clear()
        }

        override fun removeAt(index: Int): InstinctNode {
            val element = get(index)
            remove(element)
            return element
        }

        override fun remove(element: InstinctNode): Boolean {
            val result = super.remove(element)
            (element.incomingConnections + element.outgoingConnections)
                .let { element.selfConnection?.let { self -> it + self } ?: it }
                .forEach { connections -= it }
            connections.forEach { if (it.gater == element) it.gater = null }
            return result
        }
    }


    override var fitness = 0F
        set(value) {
            field = value
            score = value
        }

    /**
     * The [score] is used to compare this network with others. By default, it is set by [fitness], but can manually
     * be assigned a different value. The [InstinctPool] can be configured to apply a penalty on this network by
     * lowering its score depending on the networks size and complexity.
     */
    override var score = 0F

    override var doReset = false

    init {
        val inputNodes = mutableListOf<InputNode>()
        val outputNodes = mutableListOf<OutputNode>()

        repeat(instance.inputs) {
            inputNodes += InputNode()
        }
        repeat(instance.outputs) {
            outputNodes += OutputNode()
        }

        for (input in inputNodes) {
            for (output in outputNodes) {
                Connection(input, output, Random.nextFloat() * instance.inputs * sqrt(2F / instance.inputs))
            }
        }
    }

    /**
     * Invokes this [Network]. That means taking the given [input], processing the neural network and returning the
     * value of its output neurons.
     */
    override fun invoke(input: FloatArray): FloatArray {
        prepareNetwork()

        val output = FloatArray(instance.outputs)

        nodes.forEachIndexed { index, node ->
            if (node is InputNode) {
                node.value = node.activate(input[index])
            } else {
                node.state =
                    (node.selfConnection?.weight ?: 0F) * node.state * (node.selfConnection?.gater?.value ?: 1F)
                node.state += node.bias

                for (connection in node.incomingConnections) {
                    node.state += connection.weight * connection.from.value * (connection.gater?.value ?: 1F)
                }

                node.value = node.activate(node.state)

                if (node is OutputNode) {
                    output[index - nodes.size + instance.outputs] = node.value
                }
            }
        }

        return output
    }

    private fun prepareNetwork() {
        if (doReset) {
            doReset = false
            nodes.forEach {
                it.state = 0F
                it.value = 0F
            }
        }
    }


    /**
     * Adds a new random [Node] between two already connected nodes.
     */
    fun mutateAddNode() {
        val connection = connections.randomOrNull() ?: return
        val gater = connection.gater

        val newNode = HiddenNode(connection.toIndex, Random.nextFloat() * 2 - 1)
        val connection1 = Connection(connection.from, newNode, Random.nextFloat() * 2 - 1)
        val connection2 = Connection(newNode, connection.to, Random.nextFloat() * 2 - 1)

        listOf(connection1, connection2).random().gater = gater

        connections.remove(connection)
    }

    /**
     * Adds a new forward [Connection] between two not already connected [Node]s.
     */
    fun mutateAddForwardConnection() {
        val pairs = mutableListOf<Pair<InstinctNode, InstinctNode>>()
        for (from in nodes) {
            for (to in nodes.drop(max(instance.inputs, from.index + 1))) {
                if (to.incomingConnections.none { it.from == from }) {
                    pairs += from to to
                }
            }
        }

        if (pairs.isEmpty()) return

        val pair = pairs.random()

        Connection(pair.first, pair.second, Random.nextFloat() * 2 - 1)
    }

    /**
     * Adds a new [Connection] to and from the same [Node]. This connection is responsible for
     * "remembering" the node's [state][Node.state] between multiple [invoke] calls.
     */
    fun mutateAddSelfConnection() {
        val candidates = mutableListOf<InstinctNode>()

        for (node in nodes.drop(instance.inputs)) {
            if (node.selfConnection == null) {
                candidates += node
            }
        }

        if (candidates.isEmpty()) return

        val target = candidates.random()

        Connection(target, target, Random.nextFloat() * 2 - 1)
    }

    /**
     * Adds a new recurrent [Connection] leading back from one [Node] to another. This connection uses its
     * [from][Connection.from] node's previous [value][Node.value] when [invoking][invoke] the network.
     */
    fun mutateAddRecurrentConnection() {
        val pairs = mutableListOf<Pair<InstinctNode, InstinctNode>>()
        for (from in nodes.drop(instance.inputs)) {
            for (to in nodes.slice(instance.inputs until from.index)) {
                if (to.incomingConnections.none { it.from == from }) {
                    pairs += from to to
                }
            }
        }

        if (pairs.isEmpty()) return

        val pair = pairs.random()

        Connection(pair.first, pair.second, Random.nextFloat() * 2 - 1)
    }

    /**
     * Changes the [gate][Connection.gater] of a random [Connection].
     */
    fun mutateAddGate() {
        val connection = connections.randomOrNull() ?: return
        connection.gater = nodes.random()
    }

    /**
     * Changes the [weight][Connection.weight] of a random [Connection]. The change is in the range of [-1, 1).
     */
    fun mutateWeight() {
        val connection = connections.randomOrNull() ?: return
        connection.weight += Random.nextFloat() * 2 - 1
    }

    /**
     * Changes the [bias][Node.bias] of a random [Node]. The change is in the range of [-1, 1).
     */
    fun mutateBias() {
        val node = nodes.drop(instance.inputs).random()
        node.bias += Random.nextFloat() * 2 - 1
    }

    /**
     * Changes the [activation][Node.activate] of a random [Node]. Possible [ActivationFunction]s are defined in the
     * [instance].
     */
    fun mutateActivation() {
        val node = nodes.random()
        val activation = when (node) {
            is InputNode -> instance.inputActivations.random()
            is OutputNode -> instance.outputActivations.random()
            else -> instance.hiddenActivations.random()
        }

        node.activate = activation
    }

    /**
     * Removes a [Node]. Creates a [Connection] between every node that has been connected before.
     */
    fun mutateRemoveNode() {
        val node = nodes.drop(instance.inputs).dropLast(instance.outputs).randomOrNull() ?: return

        val gaters = mutableListOf<InstinctNode>()
        val sourceNodes = node.incomingConnections.map { connection ->
            connection.gater?.let { gaters += it }
            connection.from
        }
        val targetNodes = node.outgoingConnections.map { connection ->
            connection.gater?.let { gaters += it }
            connection.to
        }

        val newConnections = mutableListOf<InstinctConnection>()
        for (from in sourceNodes) {
            for (to in targetNodes) {
                if (from.outgoingConnections.none { it.to == to }) {
                    newConnections += Connection(from, to, Random.nextFloat() * 2 - 1)
                }
            }
        }

        for (gater in gaters) {
            if (newConnections.isEmpty()) break

            val randomConnection = newConnections.random()
            randomConnection.gater = gater
            newConnections -= randomConnection
        }

        nodes -= node
    }

    /**
     * Removes a [Connection]. Connections are not removed if they are the only connection to or from a [Node].
     */
    fun mutateRemoveConnection() {
        val connection = connections
            .filter { it.from.outgoingConnections.size > 1 && it.to.incomingConnections.size > 1 }.randomOrNull()
            ?: return
        connections -= connection
    }

    /**
     * Removes the [gate][Connection.gater] from a gated [Connection].
     */
    fun mutateRemoveGate() {
        val connection = connections.filter { it.gater != null }.randomOrNull() ?: return
        connection.gater = null
    }

    /**
     * Performs all mutations in this [InstinctNetwork] with the chances defined by the [instance].
     */
    fun mutate() {
        performMutation(instance.mutateAddNodeChance, this::mutateAddNode)
        performMutation(instance.mutateAddForwardConnectionChance, this::mutateAddForwardConnection)
        performMutation(instance.mutateAddSelfConnectionChance, this::mutateAddSelfConnection)
        performMutation(instance.mutateAddRecurrentConnectionChance, this::mutateAddRecurrentConnection)
        performMutation(instance.mutateAddGateChance, this::mutateAddGate)
        performMutation(instance.mutateWeightChance, this::mutateWeight)
        performMutation(instance.mutateBiasChance, this::mutateBias)
        performMutation(instance.mutateActivationChance, this::mutateActivation)
        performMutation(instance.mutateRemoveNodeChance, this::mutateRemoveNode)
        performMutation(instance.mutateRemoveConnectionChance, this::mutateRemoveConnection)
        performMutation(instance.mutateRemoveGateChance, this::mutateRemoveGate)
    }

    private inline fun performMutation(rawChance: Float, mutation: () -> Unit) {
        var iterations = rawChance.toInt()
        val chance = if (iterations.toFloat() < rawChance) {
            rawChance - iterations++
        } else {
            1F
        }

        repeat(iterations) {
            if (Random.nextFloat() < chance) {
                mutation()
            }
        }
    }

    /**
     * This connects two [Node]s with each other. The connection leads [from] one node [to] another one. Furthermore,
     * the [gater] is an optional node, which influences the [weight] with its [value][Node.value]. Creating
     * a new connection automatically adds it to the according [InstinctNetwork].
     */
    @Suppress("SerialVersionUIDInSerializableClass")
    inner class Connection @JvmOverloads constructor(
        val from: InstinctNode,
        val to: InstinctNode,
        var weight: Float = 0F,
        var gater: InstinctNode? = null
    ) : Serializable {
        /**
         * Creates a [Connection] between the [Node] with the index [fromIndex] and the node with the [toIndex].
         * The [gater] will be the node with the index [gaterIndex] or none if there is none with the given index.
         */
        @JvmOverloads
        constructor(fromIndex: Int, toIndex: Int, weight: Float = 0F, gaterIndex: Int = -1) : this(
            nodes[fromIndex],
            nodes[toIndex],
            weight,
            nodes.getOrNull(gaterIndex)
        )

        /**
         * The index of the [from] node.
         */
        val fromIndex get() = from.index

        /**
         * The index of the [to] node.
         */
        val toIndex get() = to.index

        /**
         * The index of the [gater] node.
         */
        var gaterIndex
            get() = gater?.index ?: -1
            set(value) {
                gater = nodes.getOrNull(value)
            }

        init {
            connections += this
            if (to != from) {
                to.incomingConnections += this
                from.outgoingConnections += this
            } else {
                from.selfConnection = this
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Connection

            if (fromIndex != other.fromIndex) return false
            if (toIndex != other.toIndex) return false

            return true
        }

        override fun hashCode(): Int {
            var result = fromIndex
            result = 31 * result + toIndex
            return result
        }
    }

    private inner class InputNode(
        activation: ActivationFunction = instance.inputActivations.random()
    ) : Node(activate = activation) {
        init {
            nodes += (this)
        }
    }

    private inner class OutputNode(
        bias: Float = 0F,
        activation: ActivationFunction = instance.outputActivations.random()
    ) : Node(bias, activation) {
        init {
            nodes += (this)
        }
    }

    /**
     * This class is a [HiddenNode] inside this [InstinctNetwork]. Upon creation, it is automatically integrated into
     * this network as a hidden node. The [index] specifies where the node should be inserted in the [nodes] list.
     * The [activation][activate] s the [ActivationFunction] used by this node.
     */
    inner class HiddenNode @JvmOverloads constructor(
        index: Int, bias: Float = 0F,
        activation: ActivationFunction = instance.hiddenActivations.random()
    ) : Node(bias, activation) {
        init {
            val cappedIndex = max(min(index, nodes.size - instance.outputs), instance.inputs)
            nodes.add(cappedIndex, this)
        }
    }

    /**
     * The [Node] is a neuron inside this network. It is connected to other neurons using [Connection]s. It has two
     * configurable parameters:
     * - [bias]: The value statically added to this neuron when [invoking][invoke] the network
     * - [activate]: The [ActivationFunction] used when calculating the [value] of this neuron
     */
    @Suppress("SerialVersionUIDInSerializableClass")
    abstract inner class Node(var bias: Float = 0F, var activate: ActivationFunction) : Serializable {
        /**
         * The [Connection] leading from and to this [Node]. This is optional so the property may be null.
         */
        var selfConnection: InstinctConnection? = null

        /**
         * A list of all incoming [Connection]s.
         */
        val incomingConnections = mutableListOf<InstinctConnection>()

        /**
         * A list of all outgoing [Connection]s.
         */
        val outgoingConnections = mutableListOf<InstinctConnection>()

        /**
         * The index of this [Node].
         */
        val index get() = nodes.indexOf(this)

        /**
         * The internal state of this [Node] before getting the result of [activate].
         */
        var state: Float = 0F

        /**
         * The value used for further calculation. By default, this is just the result of [activate] invoked with
         * [state].
         */
        var value: Float = 0F
    }

    companion object {
        private const val serialVersionUID = 0L

        /**
         * Performs a [crossover] between two [InstinctNetwork]s and [mutate]s the result before returning it. An
         * [IllegalArgumentException] is thrown if [parent1] and [parent2] are of different [InstinctInstance]s.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun offspring(parent1: InstinctNetwork, parent2: InstinctNetwork): InstinctNetwork {
            val child = crossover(parent1, parent2).apply {
                mutate()
            }
            return child
        }

        /**
         * Performs a crossover between two [InstinctNetwork]s and returns the resulting Network. [parent1] and
         * [parent2] need to be of an equal [InstinctInstance], otherwise an [IllegalArgumentException] is thrown.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun crossover(parent1: InstinctNetwork, parent2: InstinctNetwork): InstinctNetwork {
            require(parent1.instance == parent2.instance) {
                "Can not perform crossover on networks based on different InstinctInstances"
            }

            return parent1.instance.crossover(parent1, parent2)
        }

        private fun InstinctInstance.crossover(parent1: InstinctNetwork, parent2: InstinctNetwork): InstinctNetwork {
            val size = when {
                parent1.fitness > parent2.fitness -> parent1.nodes.size
                parent2.fitness > parent1.fitness -> parent2.nodes.size
                else ->
                    (min(parent1.nodes.size, parent2.nodes.size)..max(parent1.nodes.size, parent2.nodes.size)).random()
            }

            val child = Network(this)
            child.nodes.clear()

            // Nodes
            for (index in 0 until size) {
                val node =
                    if (index < size - outputs) {
                        // Non output nodes
                        if (index >= parent1.nodes.size - outputs) {
                            parent2.nodes[index]
                        } else if (index >= parent2.nodes.size - outputs) {
                            parent1.nodes[index]
                        } else {
                            listOf(parent1.nodes[index], parent2.nodes[index]).random()
                        }
                    } else {
                        listOf(
                            parent1.nodes[parent1.nodes.size - size + index],
                            parent2.nodes[parent2.nodes.size - size + index]
                        ).random()
                    }

                when (node) {
                    is InputNode -> child.InputNode(node.activate)
                    is OutputNode -> child.OutputNode(node.bias, node.activate)
                    else -> {
                        val childNode = child.HiddenNode(index, node.bias, node.activate)
                        child.nodes -= childNode
                        child.nodes.add(index, childNode)
                    }
                }
            }

            // Connections
            val parent1ConnectionsMap = parent1.connections.associateBy { it.hashCode() }
            val parent2ConnectionsMap = parent2.connections.associateBy { it.hashCode() }

            for (parentConnection in parent1ConnectionsMap.keys + parent2ConnectionsMap.keys) {
                val parent1Connection = parent1ConnectionsMap[parentConnection]
                val parent2Connection = parent2ConnectionsMap[parentConnection]
                val connection = if (parent1Connection != null && parent2Connection != null) {
                    listOf(parent1Connection, parent2Connection).random()
                } else if (parent1Connection != null && parent1.fitness >= parent2.fitness) {
                    parent1Connection
                } else if (parent2Connection != null && parent2.fitness >= parent1.fitness) {
                    parent2Connection
                } else null

                if (connection != null && connection.toIndex < size && connection.fromIndex < size) {
                    val childConnection = child.Connection(connection.fromIndex, connection.toIndex, connection.weight)
                    if (connection.gaterIndex < size) {
                        childConnection.gaterIndex = connection.gaterIndex
                    }
                }
            }

            return child
        }

        /**
         * Loads an [InstinctNetwork] from the given [input]. Can throw [ClassNotFoundException],
         * [InvalidClassException], [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
        fun load(input: InputStream): InstinctNetwork {
            val genome = ObjectInputStream(input).readObject()
            if (genome !is InstinctNetwork) {
                throw InvalidClassException(
                    "Cannot load object of type \"${genome::class.jvmName}\" as InstinctNetwork"
                )
            }
            return genome
        }

        /**
         * Loads an [InstinctNetwork] from the given [path]. Can throw [ClassNotFoundException],
         * [InvalidClassException], [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
        @Suppress("SwallowedException")
        fun load(path: String): InstinctNetwork? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }
}
