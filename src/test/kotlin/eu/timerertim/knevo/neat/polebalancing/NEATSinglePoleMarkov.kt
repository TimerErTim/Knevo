package eu.timerertim.knevo.neat.polebalancing

import eu.timerertim.knevo.shared.polebalancing.Cart
import eu.timerertim.knevo.shared.polebalancing.CartWindow
import eu.timerertim.knevo.shared.polebalancing.PoleBalancingConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() {
    val config = PoleBalancingConfig()
    val cart = Cart(config, 1F)
    cart.addPole(1F)
    //cart.addPole(0.1F)
    val window = CartWindow(cart)
    window.isVisible = true

    runBlocking {
        cart.update(10F)
        while (true) {
            val time = measureTimeMillis {
                window.repaint()
                cart.update(0F)//(Math.random() - 0.5F).toFloat() * 10)
            }
            delay((config.step * 1000 - time).toLong())
        }
    }
}