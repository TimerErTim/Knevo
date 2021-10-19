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
 * A [Population] is a [Collection] of [Genome]s. It can be used to train and evolve Genomes. For this one should invoke
 * [evaluateFitness] to determine each Genome's fitness and then [breedNewGeneration] to produce the next [generation].
 * The shorthand form is [evolve].
 */
interface Population<out G : Genome> : Collection<G>, Serializable {
    /**
     * This number keeps track of the current [generation]. It normally is increased upon invoking [breedNewGeneration].
     */
    val generation: Long


    /**
     * The [fitness][Genome.fitness] of the [best Genome][topGenome] in this [Population].
     */
    val topFitness get() = topGenome.fitness

    /**
     * The [score][Genome.score] of the [best Genome][topGenome] in this [Population].
     */
    val topScore get() = topGenome.score

    /**
     * The best performing [Genome] according to [score][Genome.score].
     */
    val topGenome get() = maxOrNull()!!

    /**
     * Evolves this [Population] using a given [environment]. It invokes [evaluateFitness] and then
     * [breedNewGeneration].
     */
    fun evolve(environment: Environment<G>) {
        evaluateFitness(environment)
        breedNewGeneration()
    }


    /**
     * Evaluates the [fitness][Genome.fitness] of every single [Genome] in this [Population] by using the given
     * [environment].
     */
    fun evaluateFitness(environment: Environment<G>)

    /**
     * Breeds a new generation based on the [evaluated Fitness][evaluateFitness]. Typically, increases [generation].
     */
    fun breedNewGeneration()

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
         * Loads a [Population] from the given [input]. Can throw [ClassNotFoundException], [InvalidClassException],
         * [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
        fun load(input: InputStream): Population<*> {
            val population = ObjectInputStream(input).readObject()
            if (population !is Population<*>) {
                throw InvalidClassException("Cannot load object of type \"${population::class.jvmName}\" as Population")
            }
            return population
        }

        /**
         * Loads a [Population] from the given [path]. Can throw [ClassNotFoundException], [InvalidClassException],
         * [StreamCorruptedException], [OptionalDataException], [IOException].
         */
        @JvmStatic
        @Throws(
            ClassNotFoundException::class, InvalidClassException::class, StreamCorruptedException::class,
            OptionalDataException::class, IOException::class
        )
        @Suppress("SwallowedException")
        fun load(path: String): Population<*>? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }
}
