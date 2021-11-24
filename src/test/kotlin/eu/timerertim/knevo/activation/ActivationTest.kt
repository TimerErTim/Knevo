package eu.timerertim.knevo.activation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.lang.Math.PI

@TestInstance(Lifecycle.PER_CLASS)
class ActivationTest {
    @Test
    fun `Sigmoid produces expected output`() {
        val sigmoid = Sigmoid()

        assertEquals(0.5F, sigmoid(0F))
        assertTrue(sigmoid(-10000F) in 0F..0.01F)
        assertTrue(sigmoid(10000F) in 0.99F..1F)
    }


    @Test
    fun `Tanh produces expected output`() {
        val tanh = Tanh()

        assertEquals(0F, tanh(0F))
        assertTrue(tanh(-10000F) in -1F..-0.99F)
        assertTrue(tanh(10000F) in 0.99F..1F)
    }

    @Test
    fun `Sinus produces expected output`() {
        val sin = Sinus()

        assertEquals(0F, sin(0F))
        assertEquals(1F, sin(0.5F * PI.toFloat()), 0.001F)
        assertEquals(-1F, sin(1.5F * PI.toFloat()), 0.001F)
        assertEquals(0F, sin(2F * PI.toFloat()), 0.001F)
    }

    @Test
    fun `Step produces expected output`() {
        val step = Step()

        assertEquals(1F, step(2F))
        assertEquals(0F, step(-1F))
        assertEquals(1F, step(0F))
        assertEquals(1F, step(-0F))
    }

    @Test
    fun `Sign produces expected output`() {
        val sign = Sign()

        assertEquals(1F, sign(1F))
        assertEquals(-1F, sign(-1F))
        assertEquals(0F, sign(0F))
        assertEquals(0F, sign(-0F), 0F)
        assertEquals(1F, sign(2F))
        assertEquals(-1F, sign(-2.0F))
    }

    @Test
    fun `Random produces random output`() {
        val random = Random()
        val results = mutableListOf<Float>()

        repeat(10) {
            results += random(1F)
        }

        assertNotEquals(1, results.toSet().size)

        repeat(10) {
            results += random(kotlin.random.Random.nextFloat())
        }

        assertNotEquals(1, results.toSet().size)
    }

    @Test
    fun `Relu produces expected output`() {
        val relu = Relu()

        assertEquals(10F, relu(10F))
        assertEquals(2F, relu(2F))
        assertEquals(1F, relu(1F))
        assertEquals(0F, relu(0F))
        assertEquals(0F, relu(-0F))
        assertEquals(0F, relu(-0.5F))
        assertEquals(0F, relu(-10000F))
    }

    @Test
    fun `Selu produces expected output`() {
        val selu = Selu()

        assertEquals(1.05F, selu(1F), 0.01F)
        assertEquals(2.1F, selu(2F), 0.01F)
        assertEquals(4.2F, selu(4F), 0.01F)
        assertTrue(selu(-0.5F) in -1F..0.5F)
        assertTrue(selu(-1.5F) in -1.5F..-1F)
        assertTrue(selu(-3F) in -2F..-1.5F)
    }

    @Test
    fun `Silu produces expected output`() {
        val silu = Silu()

        assertEquals(0.73F, silu(1F), 0.01F)
        assertEquals(1.76F, silu(2F), 0.01F)
        assertEquals(0F, silu(0F), 0F)
        assertEquals(0F, silu(-0F), 0F)
        val lowest = silu(-1.28F)
        assertTrue(silu(-1F) in lowest..0F)
        assertTrue(silu(-2F) in lowest..0F)
    }

    @Test
    fun `Linear produces expected output`() {
        val slopes = listOf(0F, 0.01F, 0.1F, 0.5F, 1F, 2F, 5F, 10F, 35.5F, 100F)
        for (slope in slopes) {
            val linear = Linear(slope)
            for (x in 0..1000)
                assertEquals(slope * x, linear(x.toFloat()))
        }
    }
}