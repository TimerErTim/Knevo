package eu.timerertim.knevo.instinct

import eu.timerertim.knevo.activation.Identity
import eu.timerertim.knevo.instinct.InstinctNetwork.Companion.crossover
import eu.timerertim.knevo.instinct.InstinctNetwork.Companion.offspring
import eu.timerertim.knevo.serialization.load
import eu.timerertim.knevo.serialization.save
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import kotlin.system.exitProcess

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstinctTest {
    @Test
    fun `Instinct serialization is working lossless`() {
        val network = Network()

        repeat(2_000) {
            network.mutateAddNode()
        }

        println("Instinct Nodes when saving: ${network.nodes.size}")

        File("out${File.separator}instinct_serialization.knv").deleteOnExit()

        assertDoesNotThrow {
            network.save("out${File.separator}instinct_serialization.knv")
            val loadedNetwork = load<Network>("out${File.separator}instinct_serialization.knv")
            assertNotNull(loadedNetwork)
            loadedNetwork ?: exitProcess(-1)
            assertEquals(network.connections, loadedNetwork.connections)
            assertArrayEquals(network(floatArrayOf(1F, 2F)), loadedNetwork(floatArrayOf(1F, 2F)))
            assertArrayEquals(network(floatArrayOf(2F, 2F)), loadedNetwork(floatArrayOf(2F, 2F)))
            assertArrayEquals(network(floatArrayOf(3F, 2F)), loadedNetwork(floatArrayOf(3F, 2F)))
        }
    }

    @Test
    fun `Instinct recurrent connections remember values between invocations`() {
        val network = Network()

        network.connections.removeAt(0)
        network.connections.removeAt(0)

        network.HiddenNode(3, 1F, activation = Identity())

        network.Connection(0, 2, 0.5F)
        network.Connection(1, 2, 0.75F)
        network.Connection(2, 3, 1F)

        network.mutateAddRecurrentConnection()


        val firstResult = network(floatArrayOf(0F, 1F))
        val secondResult = network(floatArrayOf(0F, 1F))

        assertFalse(firstResult.contentEquals(secondResult))
        assertFalse(network(floatArrayOf(2F, 1F)).contentEquals(network(floatArrayOf(2F, 1F))))

        network.doReset = true
        assertArrayEquals(firstResult, network(floatArrayOf(0F, 1F)))
        assertArrayEquals(secondResult, network(floatArrayOf(0F, 1F)))
    }

    @Test
    fun `Instinct selfconnections remember values between invocations`() {
        val network = Network()

        network.connections.removeAt(0)
        network.connections.removeAt(0)

        network.HiddenNode(3, 1F, activation = Identity())

        network.Connection(0, 2, 0.5F)
        network.Connection(1, 2, 0.75F)
        network.Connection(2, 3, 1F)

        network.mutateAddSelfConnection()


        val firstResult = network(floatArrayOf(0F, 1F))
        val secondResult = network(floatArrayOf(0F, 1F))

        assertFalse(firstResult.contentEquals(secondResult))
        assertFalse(network(floatArrayOf(2F, 1F)).contentEquals(network(floatArrayOf(2F, 1F))))

        network.doReset = true
        assertArrayEquals(firstResult, network(floatArrayOf(0F, 1F)))
        assertArrayEquals(secondResult, network(floatArrayOf(0F, 1F)))
    }

    @Test
    fun `Instinct only forward connection returns same value `() {
        val network = Network()

        network.connections.removeAt(0)
        network.connections.removeAt(0)

        network.HiddenNode(3, 1F, activation = Identity())

        network.Connection(0, 2, 0.5F)
        network.Connection(1, 2, 0.75F)
        network.Connection(2, 3, 1F)


        val firstResult = network(floatArrayOf(0F, 1F))
        val secondResult = network(floatArrayOf(0F, 1F))

        assertTrue(firstResult.contentEquals(secondResult))
        assertTrue(network(floatArrayOf(2F, 1F)).contentEquals(network(floatArrayOf(2F, 1F))))

        network.doReset = true
        assertArrayEquals(firstResult, network(floatArrayOf(0F, 1F)))
        assertArrayEquals(secondResult, network(floatArrayOf(0F, 1F)))
    }

    @Test
    fun `Mutation limits are respected and do not cause a crash`() {
        repeat(100) {
            val network = Network()

            val tempResult = network(floatArrayOf(1F, 0F))

            assertDoesNotThrow {
                repeat(20) {
                    network.mutateRemoveConnection()
                    network.mutateRemoveGate()
                    network.mutateRemoveNode()
                }
            }

            assertArrayEquals(tempResult, network(floatArrayOf(1F, 0F)))

            assertDoesNotThrow {
                repeat(20) {
                    network.mutateAddNode()
                    network.mutateAddForwardConnection()
                    network.mutateAddRecurrentConnection()
                    network.mutateAddSelfConnection()
                    network.mutateAddGate()
                    network.mutateWeight()
                    network.mutateBias()
                    network.mutateActivation()
                }

                repeat(10) {
                    network.mutateRemoveGate()
                }

                repeat(50) {
                    network.mutateRemoveConnection()
                }

                repeat(50) {
                    network.mutateRemoveGate()
                }

                repeat(10) {
                    network.mutateRemoveNode()
                }

                repeat(50) {
                    network.mutateAddGate()
                }

                repeat(50) {
                    network.mutateRemoveNode()
                }

                repeat(50) {
                    network.mutateRemoveConnection()
                }
            }

            assertEquals(3, network.nodes.size)
            assertEquals(2, network.connections.size)

            assertDoesNotThrow {
                network.doReset = true
                val copiedNetwork = Network(network)
                val crossoverNetwork = crossover(copiedNetwork, network)

                val networkOutput = network(floatArrayOf(1F, 2F))
                val copiedNetworkOutput = copiedNetwork(floatArrayOf(1F, 2F))
                val crossoverNetworkOutput = crossoverNetwork(floatArrayOf(1F, 2F))
                assertArrayEquals(networkOutput, copiedNetworkOutput)
                assertArrayEquals(networkOutput, crossoverNetworkOutput)
                assertArrayEquals(copiedNetworkOutput, crossoverNetworkOutput)
            }
        }
    }

    @Test
    fun `Instinct large networks do not crash`() {
        val instance = InstinctInstance(100, 250)
        val network = instance.Network()

        assertDoesNotThrow {
            repeat(500) {
                network.mutate()
            }
            network(FloatArray(100) { 10F })

            val offspringNetwork = offspring(instance.Network(), network)
            val clonedNetwork = Network(offspringNetwork)
            assertArrayEquals(offspringNetwork(FloatArray(100)), clonedNetwork(FloatArray(100)))
            assertArrayEquals(offspringNetwork(FloatArray(100)), clonedNetwork(FloatArray(100)))
        }
    }
}
