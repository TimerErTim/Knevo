package eu.timerertim.knevo.shared.car

import eu.timerertim.knevo.Genome
import eu.timerertim.knevo.Population
import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

fun run(pool: Population<Genome>, interval: Int, baseTitle: String) {
    val car = Car()
    if (interval == -1) throw AssertionError()
    val environment = CarTraining(car, car.backgroundImage, interval, pool, baseTitle)

    val fileWriter = File(
        "out/${baseTitle.lowercase(Locale.getDefault()).replace(" ", "_")}.csv"
    ).also { it.parentFile.mkdirs() }.printWriter()

    var topGenome: Genome
    fileWriter.use {
        it.println("generation;fitness;time")
        val generation by pool::generation
        var time: Long

        do {
            time = measureTimeMillis {
                pool.evaluateFitness(environment)
            }

            topGenome = pool.topGenome
            topGenome.doReset = true
            if (generation % interval == 0L) environment.showGenome(topGenome)

            time += measureTimeMillis {
                pool.breedNewGeneration()
            }

            println("Time taken for generation ${generation - 1}: ${time / 1000F} seconds")
            it.println("${generation - 1};${topGenome.fitness};$time")
        } while (topGenome.score < 6250 && generation < 25000)
    }

    environment.showGenome(topGenome, true)
}
