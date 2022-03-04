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

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import javax.imageio.ImageIO
import javax.swing.JPanel

class Frame(car: Car) : JPanel() {
    private val carObject: Car
    var carLocation = CarLocation()
        private set

    fun setLocation(location: CarLocation) {
        carLocation = location
    }

    var car: BufferedImage? = null
    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        draw(g)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(800, 800)
    }

    private fun draw(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.drawImage(backgroundImage, 0, 0, null)
        val transform = AffineTransform()
        transform.rotate(Math.toRadians(carLocation.angle.toDouble()), carLocation.x, carLocation.y)
        val old = g2d.transform
        g2d.transform(transform)
        g2d.drawImage(
            car,
            carLocation.x.toInt() - 30,
            carLocation.y.toInt() - 15,
            60,
            30,
            null
        )
        g2d.transform = old
        for (ant in carLocation.antennas) {
            ant!!.draw(backgroundImage!!, g2d)
        }
        g2d.color = Color.WHITE
        g2d.font = g2d.font.deriveFont(20f)
        g2d.drawString("Speed " + round(carLocation.currentSpeed * 100, 3) + "%", 10, 25)
    }

    val backgroundImage: BufferedImage?
        get() = carObject.backgroundImage

    companion object {
        private const val serialVersionUID = 1L
        fun round(value: Double, places: Int): Double {
            return BigDecimal(value).setScale(places, RoundingMode.HALF_UP).toDouble()
        }
    }

    init {
        carObject = car
        background = Color.BLACK
        isFocusable = true
    }

    init {
        try {
            this.car = ImageIO.read(this.javaClass.getResource("/car/car.png"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}