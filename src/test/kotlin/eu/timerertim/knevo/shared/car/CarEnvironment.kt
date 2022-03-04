/**
 * Copyright 2016 Alexander Gielisse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.timerertim.knevo.shared.car

import eu.timerertim.knevo.Environment
import eu.timerertim.knevo.Genome
import eu.timerertim.knevo.Population
import java.awt.image.BufferedImage
import javax.swing.JFrame

class CarTraining(
    private val car: Car,
    private val background: BufferedImage?,
    private val interval: Int,
    private val pool: Population<Genome>,
    private val baseTitle: String
) : Environment<Genome> {
    private val generation by pool::generation
    private var board: Frame

    var title: String? by car::title

    fun showGenome(topGenome: Genome, loop: Boolean = false) {
        title = "$baseTitle - Generation " + generation + " - Fitness " + topGenome.score
        board.setLocation(CarLocation())
        var death = 0
        while (true) {
            try {
                Thread.sleep((1000.0 / 30.0).toLong()) // 30 FPS
                var rightClicked = false
                var leftClicked = false
                val inputs = FloatArray(board.carLocation.antennas.size + 1)
                for (i in board.carLocation.antennas.indices) {
                    val ant = board.carLocation.antennas[i]
                    var len = ant!!.getFreeDistance(background!!).toFloat()
                    if (len > 200) len = 200f
                    inputs[i] = len / 200f
                }
                inputs[inputs.size - 1] = board.carLocation.currentSpeed.toFloat()
                val ans = topGenome(inputs)
                val output = ans[0].toDouble()
                val speed = ans[1].toDouble()
                if (output in 0.0..0.3) leftClicked = true
                if (output in 0.7..1.0) rightClicked = true
                board.carLocation.tick(rightClicked, leftClicked, speed)
                if (!board.carLocation.isAlive(board.backgroundImage)) {
                    // restart
                    board.setLocation(CarLocation())
                    death++
                    continue
                }
                if (board.carLocation.isOnFinish(board.backgroundImage)) {
                    if (!loop) break else {
                        board.setLocation(CarLocation())
                        continue
                    }
                }
                if (death >= 1 && !loop) {
                    break
                }
                car.repaint()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun evaluateFitness(population: List<Genome>) {
        for (gene in population) {
            val carLocation = CarLocation()
            var ticksLived: Long = 0
            while (carLocation.isAlive(background) && !carLocation.isOnFinish(background)) {
                var rightClicked = false
                var leftClicked = false
                val inputs = FloatArray(carLocation.antennas.size + 1)
                for (i in carLocation.antennas.indices) {
                    val ant = carLocation.antennas[i]
                    var len = ant!!.getFreeDistance(background!!).toFloat()
                    if (len > 200) len = 200f
                    inputs[i] = len / 200f
                }
                inputs[inputs.size - 1] = carLocation.currentSpeed.toFloat()
                val ans = gene(inputs)
                val output = ans[0].toDouble()
                val speed = ans[1].toDouble()
                if (output in 0.0..0.3) leftClicked = true
                if (output in 0.7..1.0) rightClicked = true
                carLocation.tick(rightClicked, leftClicked, speed)
                ticksLived++
            }

            /**
             * First 50 fitness is for actually making it (0-100%), rest is for speed.
             */
            val secondsLived = ticksLived / 30f // 30 ticks per second
            if (secondsLived > 45) throw RuntimeException()
            val fitness = if (carLocation.isOnFinish(background)) {
                // we finished
                45 + (45 - secondsLived)
            } else {
                secondsLived
            }
            gene.fitness = fitness
        }
    }

    init {
        car.add(Frame(car).also { board = it })
        car.pack()
        car.setLocationRelativeTo(null)
        car.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        car.isVisible = true
        car.title = baseTitle
        car.isResizable = false
    }
}
