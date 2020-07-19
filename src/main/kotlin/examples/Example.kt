package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool

fun main() {
    val xor = XOR()

    val pool = Pool()
    pool.initializePool()

    var topGenome: Genome
    var generation = 0

    do {
        pool.evaluateFitness(xor)
        topGenome = pool.topGenome

        println("TopFitness : " + topGenome.points)
        println("Generation : $generation")

        pool.breedNewGeneration()
        generation++
    } while (topGenome.points < 15)

    println(pool.topGenome.evaluateNetwork(floatArrayOf(1f, 0f))[0])
}

class XOR : Environment {

    override fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            var fitness = 0f
            gene.fitness = 0f
            for (i in 0..1)
                for (j in 0..1) {
                    val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                    val output = gene.evaluateNetwork(inputs)
                    val expected = i xor j
                    fitness += 1 - Math.abs(expected - output[0])
                }
            fitness *= fitness

            gene.fitness = fitness
        }
    }
}