package eu.timerertim.knevo

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OptionalDataException
import java.io.OutputStream
import java.io.Serializable
import java.io.StreamCorruptedException
import kotlin.reflect.jvm.jvmName

interface Genome : Comparable<Genome>, (FloatArray) -> FloatArray, Serializable {
    /**
     * The fitness evaluated by an [Environment].
     */
    var fitness: Float

    /**
     * The modified [fitness] which is used for comparing different [Genome]s.
     * In many cases this is equal to fitness, but keep in mind that depending on configuration and algorithm it might
     * differ.
     */
    val score: Float get() = fitness
    var doReset: Boolean

    override fun compareTo(other: Genome) = this.score.compareTo(other.score)


    // Serialization
    fun save(output: OutputStream) = ObjectOutputStream(output).writeObject(this)
    fun save(path: String) = save(FileOutputStream(path))

    companion object {
        @JvmStatic
        fun load(input: InputStream): Genome {
            val genome = ObjectInputStream(input).readObject()
            if (genome !is Genome) {
                throw InvalidClassException("Cannot load object of type \"${genome::class.jvmName}\" as Genome")
            }
            return genome
        }

        @JvmStatic
        @Suppress("SwallowedException")
        fun load(path: String): Genome? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }
}
