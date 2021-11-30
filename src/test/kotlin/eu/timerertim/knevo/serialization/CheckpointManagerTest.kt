package eu.timerertim.knevo.serialization

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.io.File.separator
import kotlin.random.Random

@TestInstance(Lifecycle.PER_CLASS)
class CheckpointManagerTest {
    @AfterAll
    @BeforeAll
    fun `Remove files`() {
        File("out").deleteRecursively()
    }

    @Test
    fun `Saving increases number without exception`() {
        val directory = "out${separator}increase"
        val manager = CheckpointManager<ArrayList<Int>>(directory, 4)
        val expectedNumbers = listOf(0, 1, 2, 3, 4, 5)

        val actualNumbers = mutableListOf<Int>()

        repeat(6) {
            actualNumbers += manager.save(ArrayList())
        }

        assertEquals(expectedNumbers, actualNumbers)
        assertTrue(File(directory, "ckpt_5.knv").exists())
        assertEquals(4, File(directory).listFiles()?.size)
    }

    @Test
    fun `Saving increases number based on files`() {
        val directory = "out${separator}increaseFromFile"
        val manager = CheckpointManager<ArrayList<Int>>(directory, 4)

        repeat(2) {
            manager.save(ArrayList())
        }

        File(directory, "ckpt_1.knv").deleteRecursively()

        assertEquals(1, manager.save(ArrayList()))
    }

    @Test
    fun `Saving deletes files not based on number`() {
        val directory = "out${separator}deleteFromFile"
        val manager = CheckpointManager<String>(directory, 3)

        repeat(3) {
            manager.save("")
        }

        File(directory, "ckpt_1.knv").deleteRecursively()
        assertEquals(3, manager.save(""))
        assertTrue(File(directory, "ckpt_0.knv").exists())
        assertEquals(3, File(directory).listFiles()?.size)
    }

    @Test
    fun `Loading returns last checkpoint based on files or null`() {
        val directory = "out${separator}loading"
        val manager = CheckpointManager<String>(directory, 3)
        val generatedStrings = mutableListOf<String>()

        repeat(3) {
            val string = String.random()
            generatedStrings += string
            manager.save(string)
        }

        assertEquals(generatedStrings.last(), manager.load())
        File(directory, "ckpt_2.knv").deleteRecursively()
        assertEquals(generatedStrings[1], manager.load())
        val string = String.random()
        manager.save(string)
        assertEquals(string, manager.load())
        File(directory, "ckpt_0.knv").deleteRecursively()
        assertEquals(string, manager.load())
    }

    private val alphanumeric = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    fun String.Companion.random() = (1..((Random.nextFloat() * 100).toInt() + 10))
        .map { Random.nextInt(0, alphanumeric.size) }
        .map(alphanumeric::get)
        .joinToString("")
}
