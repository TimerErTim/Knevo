package eu.timerertim.knevo

import java.io.*
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
            check(genome is Genome) {
                "Cannot load object of type \"${genome::class.jvmName}\" as Genome"
            }
            return genome
        }

        @JvmStatic
        fun load(path: String): Genome? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }
}