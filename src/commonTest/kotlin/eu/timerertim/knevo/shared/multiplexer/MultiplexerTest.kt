package eu.timerertim.knevo.shared.multiplexer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class MultiplexerTest {
    @Test
    fun `Total bits add up`() {
        var multiplexer = Multiplexer(2)
        assertEquals(6, multiplexer.totalBits)

        multiplexer = Multiplexer(3)
        assertEquals(11, multiplexer.totalBits)
    }

    @Test
    fun `Multiplexer produces right result`() {
        val multiplexer = Multiplexer(2)
        assertEquals(true, multiplexer(booleanArrayOf(false, false, false, false, false, true)))
        assertEquals(true, multiplexer(booleanArrayOf(true, false, false, true, true, false)))
        assertEquals(false, multiplexer(booleanArrayOf(true, true, false, true, true, true)))
    }
}
