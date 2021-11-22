package eu.timerertim.knevo.serialization

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.io.File.separator
import kotlin.random.Random

@TestInstance(Lifecycle.PER_CLASS)
class SerializationTest {
    @AfterAll
    @BeforeAll
    fun `Remove files`() {
        File("out").deleteRecursively()
    }

    @Test
    fun `Saving creates required directories`() {
        val path = "out${separator}sub${separator}directory${separator}dest.knv"
        "".save(path)
        assertTrue(File(path).exists())
    }

    @Test
    fun `Load from save returns saved value`() {
        val path = "out${separator}string_list.knv"
        val list = ArrayList<String>()
        val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        repeat((Random.nextFloat() * 1000).toInt()) {
            list += (1..((Random.nextFloat() * 100).toInt() + 10))
                .map { Random.nextInt(0, alphanumeric.size) }
                .map(alphanumeric::get)
                .joinToString("")
        }

        list.save(path)
        val loadedList: ArrayList<String>? = load(path)
        if (loadedList == null) {
            assertNotNull(loadedList)
            return
        }

        assertEquals(list.toList(), loadedList)
    }

    @Test
    fun `Load from nonexistent file returns null`() {
        val path = "out${separator}no${separator}file.dum"
        assertNull(load(path))
    }
}