package eu.timerertim.knevo

import java.io.*
import kotlin.reflect.jvm.jvmName

interface Population<out G : Genome> : Collection<G>, Serializable {
    var generation: Long

    val topFitness get() = maxOf { it.fitness }
    val topScore get() = maxOf { it.score }
    val topGenome get() = maxOrNull()!!

    fun evolve(environment: Environment<G>) {
        breedNewGeneration()
        evaluateFitness(environment)
    }

    fun evaluateFitness(environment: Environment<G>)
    fun breedNewGeneration()

    // Serialization
    fun save(output: OutputStream) = ObjectOutputStream(output).writeObject(this)
    fun save(path: String) = save(FileOutputStream(path))

    companion object {
        @JvmStatic
        fun load(input: InputStream): Population<*> {
            val population = ObjectInputStream(input).readObject()
            check(population is Population<*>) {
                "Cannot load object of type \"${population::class.jvmName}\" as Population"
            }
            return population
        }

        @JvmStatic
        fun load(path: String): Population<*>? {
            return try {
                load(FileInputStream(path))
            } catch (ex: FileNotFoundException) {
                null
            }
        }
    }
}