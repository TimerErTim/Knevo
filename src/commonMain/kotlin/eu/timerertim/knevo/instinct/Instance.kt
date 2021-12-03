@file:JvmName("Instinct")
@file:JvmMultifileClass

package eu.timerertim.knevo.instinct

import eu.timerertim.knevo.activation.ActivationFunction
import eu.timerertim.knevo.activation.Identity
import eu.timerertim.knevo.activation.Linear
import eu.timerertim.knevo.activation.Relu
import eu.timerertim.knevo.activation.Selu
import eu.timerertim.knevo.activation.Sigmoid
import eu.timerertim.knevo.activation.Sign
import eu.timerertim.knevo.activation.Silu
import eu.timerertim.knevo.activation.Sinus
import eu.timerertim.knevo.activation.Step
import eu.timerertim.knevo.activation.Tanh
import eu.timerertim.knevo.selection.SelectionFunction
import java.io.Serializable
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

typealias InstinctInstanceBuilder = InstinctInstance.Builder

/**
 * The global [InstinctInstance] which is used when no other InstinctInstance is specified.
 * For example:
 *
 *      val n = InstinctNetwork()
 *      val n = globalInstinctInstance.Network()
 *
 *  does the same.
 */
var globalInstinctInstance = InstinctInstance(2, 1)

/**
 * The main class when interacting with the
 * [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) implementation in Knevo.
 *
 * It contains every necessary configuration information needed for basic usage of this neuroevolution algorithm.
 *
 * The algorithm features:
 * - Crossover
 * - Mutation
 * - Complexification
 * - Simplification
 * - Recurrent Connections
 * - Gated Connections
 * - Mutable [ActivationFunction]s
 * - Different [SelectionFunction]s
 * - Training Penalties (regarding growth/size of network)
 *
 * Configurable parameters:
 * - [inputs]: The amount of input neurons
 * - [outputs]: The amount of output neurons
 * - [inputActivations]: A list of possible ActivationFunctions for input neurons
 * - [outputActivations]: A list of possible ActivationFunctions for output neurons
 * - [hiddenActivations]: A list of possible ActivationFunctions for hidden neurons
 * - [mutateAddNodeChance]: The chance of adding a new hidden neuron
 * - [mutateAddForwardConnectionChance]: The chance of adding a forward connection
 * - [mutateAddSelfConnectionChance]: The chance of adding a connection from and to the same neuron
 * - [mutateAddRecurrentConnectionChance]: The chance of adding a recurrent connection
 * - [mutateAddGateChance]: The chance of adding a gate neuron to an existing connection
 * - [mutateWeightChance]: The chance of mutating the weight of an existing connection
 * - [mutateBiasChance]: The chance of mutating the bias of an existing neuron
 * - [mutateActivationChance]: The chance of changing the ActivationFunction of a neuron
 * - [mutateRemoveNodeChance]: The chance of removing an existing hidden neuron
 * - [mutateRemoveConnectionChance]: The chance of removing an existing connection
 * - [mutateRemoveGateChance]: The chance of removing the gate neuron from an existing connection with gate neuron
 *
 * Every mutation chance consists of the number before the decimal point and one after the decimal point. The first
 * number increased by one equals the amount of mutation tries. The one after the decimal point determines the chance of
 * the mutation actually occurring per try.
 *
 *      mutateAddNodeChance = 1.05F // 2 tries, 5% chance each
 *      mutateRemoveConnectionChance = 0.10F // 1 try, 10% chance
 *      mutateAddRecurrentConnectionChance = 0.0F // No try, 0% chance
 */
data class InstinctInstance @JvmOverloads constructor(
    val inputs: Int,
    val outputs: Int,

    val inputActivations: List<ActivationFunction> = listOf(Identity()),
    val outputActivations: List<ActivationFunction> = listOf(Identity()),
    val hiddenActivations: List<ActivationFunction> =
        listOf(Sigmoid(), Tanh(), Step(), Sign(), Linear(), Sinus(), Relu(), Selu(), Silu()),

    val mutateAddNodeChance: Float = 1.05F,
    val mutateAddForwardConnectionChance: Float = 5.20F,
    val mutateAddSelfConnectionChance: Float = 1.025F,
    val mutateAddRecurrentConnectionChance: Float = 2.01F,
    val mutateAddGateChance: Float = 2.15F,
    val mutateWeightChance: Float = 5.75F,
    val mutateBiasChance: Float = 2.50F,
    val mutateActivationChance: Float = 0.01F,
    val mutateRemoveNodeChance: Float = 0.025F,
    val mutateRemoveConnectionChance: Float = 0.10F,
    val mutateRemoveGateChance: Float = 1.10F
) : Serializable {
    private companion object {
        private const val serialVersionUID = 0L
    }

    /**
     * This class is used for building an [InstinctInstance]. The most mandatory properties are [inputs] and [outputs].
     */
    data class Builder(
        var inputs: Int,
        var outputs: Int,
    ) {

        /**
         * A list of possible [ActivationFunction]s for input neurons.
         */
        var inputActivations: List<ActivationFunction>? = null

        /**
         * A list of possible [ActivationFunction]s for output neurons.
         */
        var outputActivations: List<ActivationFunction>? = null

        /**
         * A list of possible [ActivationFunction]s for hidden neurons.
         */
        var hiddenActivations: List<ActivationFunction>? = null


        /**
         * The chance of adding a new hidden neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateAddNodeChance: Float? = null

        /**
         * The chance of adding a forward connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateAddForwardConnectionChance: Float? = null

        /**
         * The chance of adding a connection from and to the same neuron.
         *
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateAddSelfConnectionChance: Float? = null

        /**
         * The chance of adding a recurrent connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateAddRecurrentConnectionChance: Float? = null

        /**
         * The chance of adding a gate neuron to an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateAddGateChance: Float? = null

        /**
         * The chance of mutating the weight of an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateWeightChance: Float? = null

        /**
         * The chance of mutating the bias of an existing neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateBiasChance: Float? = null

        /**
         * The chance of changing the ActivationFunction of a neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateActivationChance: Float? = null

        /**
         * The chance of removing an existing hidden neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateRemoveNodeChance: Float? = null

        /**
         * The chance of removing an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateRemoveConnectionChance: Float? = null

        /**
         * The chance of removing the gate neuron from an existing connection with gate neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         */
        var mutateRemoveGateChance: Float? = null


        /**
         * Changes the amount of input neurons. Returns this [Builder] to allow chaining.
         */
        fun inputs(value: Int) = apply { inputs = value }

        /**
         * Changes the amount of output neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun outputs(value: Int) = apply { outputs = value }

        /**
         * Changes the list of possible [ActivationFunction]s for input neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun inputActivations(value: List<ActivationFunction>?) = apply { inputActivations = value }

        /**
         * Changes the list of possible [ActivationFunction]s for output neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun outputActivations(value: List<ActivationFunction>?) = apply { outputActivations = value }

        /**
         * Changes the list of possible [ActivationFunction]s for hidden neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun hiddenActivations(value: List<ActivationFunction>?) = apply { hiddenActivations = value }

        /**
         * Changes the chance of adding a new hidden neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateAddNodeChance(value: Float?) = apply { mutateAddNodeChance = value }

        /**
         * Changes the chance of adding a forward connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateAddForwardConnectionChance(value: Float?) = apply { mutateAddForwardConnectionChance = value }

        /**
         * Changes the chance of adding a connection from and to the same neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateAddSelfConnectionChance(value: Float?) = apply { mutateAddSelfConnectionChance = value }

        /**
         * Changes the chance of adding a recurrent connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateAddRecurrentConnectionChance(value: Float?) =
            apply { mutateAddRecurrentConnectionChance = value }

        /**
         * Changes the chance of adding a gate neuron to an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateAddGateChance(value: Float?) = apply { mutateAddGateChance = value }

        /**
         * Changes the chance of mutating the weight of an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateWeightChance(value: Float?) = apply { mutateWeightChance = value }

        /**
         * Changes the chance of mutating the bias of an existing neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateBiasChance(value: Float?) = apply { mutateBiasChance = value }

        /**
         * Changes the chance of changing the ActivationFunction of a neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateActivationChance(value: Float?) = apply { mutateActivationChance = value }

        /**
         * Changes the chance of removing an existing hidden neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateRemoveNodeChance(value: Float?) = apply { mutateRemoveNodeChance = value }

        /**
         * Changes the chance of removing an existing connection.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateRemoveConnectionChance(value: Float?) = apply { mutateRemoveConnectionChance = value }

        /**
         * Changes the chance of removing the gate neuron from an existing connection with gate neuron.
         * Please read the documentation of [InstinctInstance] to understand how these chances work.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun mutateRemoveGateChance(value: Float?) = apply { mutateRemoveGateChance = value }

        /**
         * Changes the list of possible [ActivationFunction]s for input neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun inputActivations(vararg activations: ActivationFunction) = apply { inputActivations = activations.asList() }

        /**
         * Changes the list of possible [ActivationFunction]s for output neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun outputActivations(vararg activations: ActivationFunction) =
            apply { outputActivations = activations.asList() }

        /**
         * Changes the list of possible [ActivationFunction]s for hidden neurons.
         *
         * Returns this [Builder] to allow chaining.
         */
        fun hiddenActivations(vararg activations: ActivationFunction) =
            apply { hiddenActivations = activations.asList() }

        /**
         * Builds an [InstinctInstance] based on the properties of this [Builder]. Unspecified/null properties will
         * have the default values defined in the InstinctInstance constructor.
         */
        fun build(): InstinctInstance {
            val constructor = InstinctInstance::class.primaryConstructor!!
            val values = Builder::class.declaredMemberProperties
            val valueMap = constructor.valueParameters.associateWith { parameter ->
                values.first { it.name == parameter.name }.get(this)
            }.filter {
                it.value != null
            }
            return constructor.callBy(valueMap)
        }
    }
}
