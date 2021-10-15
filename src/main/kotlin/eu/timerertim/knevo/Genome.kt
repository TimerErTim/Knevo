package eu.timerertim.knevo

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InvalidClassException
import java.io.NotSerializableException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OptionalDataException
import java.io.OutputStream
import java.io.Serializable
import java.io.StreamCorruptedException
import kotlin.reflect.jvm.jvmName

/**
 * A [Genome] actually is the phenotype of a neuroevolution algorithm. It can be invoked using a [FloatArray] as input
 * while returning its output as FloatArray. Furthermore, it provides common properties and functions, which are all
 * shared by different algorithm implementations.
 */
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

    /**
     * If this is true upon next [invoke], this [Genome] is reset and forgets all its previous states.
     */
    var doReset: Boolean

    /**
     * Compares this and one [other] [Genome] based on their [score][Genome.score].
     */
    override operator fun compareTo(other: Genome) = this.score.compareTo(other.score)

    /**
     * Invokes this [Genome]. That means taking the given [input], processing it (typically running through an
     * ANN) and returning the result.
     */
    override fun invoke(input: FloatArray): FloatArray

    // Serialization
    /**
     * Saves this [Genome] to the given [output]. Can throw [InvalidClassException], [NotSerializableException],
     * [IOException].
     */
    @Throws(InvalidClassException::class, NotSerializableException::class, IOException::class)
    fun save(output: OutputStream) = ObjectOutputStream(output).writeObject(this)

    /**
     * Saves this [Genome] to the given [path]. Can throw [InvalidClassException], [NotSerializableException],
     * [IOException].
     */
    @Throws(InvalidClassException::class, NotSerializableException::class, IOException::class)
    fun save(path: String) = save(FileOutputStream(path))

    companion object {
        /**
         * Loads a [Genome] from the given [input]. Can throw [ClassNotFoundException], [InvalidClassException],
         * [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
        fun load(input: InputStream): Genome {
            val genome = ObjectInputStream(input).readObject()
            if (genome !is Genome) {
                throw InvalidClassException("Cannot load object of type \"${genome::class.jvmName}\" as Genome")
            }
            return genome
        }

        /**
         * Loads a [Genome] from the given [path]. Can throw [ClassNotFoundException], [InvalidClassException],
         * [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
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
