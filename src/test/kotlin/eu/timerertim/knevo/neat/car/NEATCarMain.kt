package examples

import eu.timerertim.knevo.neat.Pool
import eu.timerertim.knevo.neat.config.NEATConfig
import eu.timerertim.knevo.shared.car.Car
import eu.timerertim.knevo.shared.car.CarLocation


fun main() {
    val config = NEATConfig.Builder()
        .setPopulationSize(200)
        .setBatchSize(50)
        .setInputs(CarLocation().antennas.size + 1)
        .setOutputs(2)
        .build()

    val pool = Pool(config)
    pool.initializePool()


    Car().run(pool)
}
