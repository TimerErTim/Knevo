package eu.timerertim.knevo.instinct

import eu.timerertim.knevo.activation.*
import java.io.Serializable
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

typealias InstinctInstanceBuilder = InstinctInstance.Builder

var DefaultInstinctInstance = InstinctInstance(2, 1)

data class InstinctInstance @JvmOverloads constructor(
    val inputs: Int,
    val outputs: Int,

    val inputActivations: List<ActivationFunction> = listOf(Identity()),
    val outputActivations: List<ActivationFunction> = listOf(Identity()),
    val hiddenActivations: List<ActivationFunction> =
        listOf(Sigmoid(), Tanh(), Step(), Sign(), Random(), Linear(), Sinus(), Relu(), Selu(), Silu()),

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
    companion object {
        private const val serialVersionUID = 0L
    }

    data class Builder(
        var inputs: Int,
        var outputs: Int,
    ) {
        var inputActivations: List<ActivationFunction>? = null
        var outputActivations: List<ActivationFunction>? = null
        var hiddenActivations: List<ActivationFunction>? = null

        var mutateAddNodeChance: Float? = null
        var mutateAddForwardConnectionChance: Float? = null
        var mutateAddSelfConnectionChance: Float? = null
        var mutateAddRecurrentConnectionChance: Float? = null
        var mutateAddGateChance: Float? = null
        var mutateWeightChance: Float? = null
        var mutateBiasChance: Float? = null
        var mutateActivationChance: Float? = null
        var mutateRemoveNodeChance: Float? = null
        var mutateRemoveConnectionChance: Float? = null
        var mutateRemoveGateChance: Float? = null

        fun inputs(value: Int) = apply { inputs = value }
        fun outputs(value: Int) = apply { outputs = value }
        fun inputActivations(value: List<ActivationFunction>?) = apply { inputActivations = value }
        fun outputActivations(value: List<ActivationFunction>?) = apply { outputActivations = value }
        fun hiddenActivations(value: List<ActivationFunction>?) = apply { hiddenActivations = value }
        fun mutateAddNodeChance(value: Float?) = apply { mutateAddNodeChance = value }
        fun mutateAddForwardConnectionChance(value: Float?) = apply { mutateAddForwardConnectionChance = value }
        fun mutateAddSelfConnectionChance(value: Float?) = apply { mutateAddSelfConnectionChance = value }
        fun mutateAddRecurrentConnectionChance(value: Float?) =
            apply { mutateAddRecurrentConnectionChance = value }

        fun mutateAddGateChance(value: Float?) = apply { mutateAddGateChance = value }
        fun mutateWeightChance(value: Float?) = apply { mutateWeightChance = value }
        fun mutateBiasChance(value: Float?) = apply { mutateBiasChance = value }
        fun mutateActivationChance(value: Float?) = apply { mutateActivationChance = value }
        fun mutateRemoveNodeChance(value: Float?) = apply { mutateRemoveNodeChance = value }
        fun mutateRemoveConnectionChance(value: Float?) = apply { mutateRemoveConnectionChance = value }
        fun mutateRemoveGateChance(value: Float?) = apply { mutateRemoveGateChance = value }

        fun inputActivations(vararg activations: ActivationFunction) = apply { inputActivations = activations.asList() }
        fun outputActivations(vararg activations: ActivationFunction) =
            apply { outputActivations = activations.asList() }

        fun hiddenActivations(vararg activations: ActivationFunction) =
            apply { hiddenActivations = activations.asList() }

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
