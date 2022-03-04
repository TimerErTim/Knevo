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
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sin

class CarLocation @JvmOverloads constructor(var x: Double = startX, var y: Double = startY, var angle: Float = 0f) {
    val antennas: MutableList<Antenna?> = ArrayList()

    private var _currentSpeed = 5.0
    val currentSpeed get() = _currentSpeed / MAX_CAR_SPEED

    fun tick(
        rightClicked: Boolean,
        leftClicked: Boolean,
        gasPercentage: Double /*0 = no has, 0.5 = same speed , 1 = full gas*/
    ) {
        val gasPercentage = gasPercentage.coerceAtMost(1.0).coerceAtLeast(0.0)

        if (leftClicked) {
            angle -= 3.5.toFloat()
        }
        if (rightClicked) {
            angle += 3.5.toFloat()
        }
        val gasChange = (gasPercentage - 0.5) * 0.3
        _currentSpeed += gasChange
        if (_currentSpeed < 5) {
            _currentSpeed = 5.0
        }
        if (_currentSpeed > MAX_CAR_SPEED) _currentSpeed = MAX_CAR_SPEED

        // update the x and y using angle and speed
        val dx = _currentSpeed * cos(Math.toRadians(angle.toDouble()))
        val dy = _currentSpeed * sin(Math.toRadians(angle.toDouble()))
        x += dx
        y += dy
    }

    fun isAlive(background: BufferedImage?): Boolean {
        val color = background?.getRGB(x.toInt(), y.toInt())
        return color == Antenna.ROAD_COLOR.rgb || color == Color.RED.rgb
    }

    fun isOnFinish(background: BufferedImage?): Boolean {
        return background!!.getRGB(x.toInt(), y.toInt()) == Color.RED.rgb
    }

    companion object {
        private const val startX = 400.0
        private const val startY = 460.0
        private const val MAX_CAR_SPEED = 25.0
    }

    init {
        antennas.add(Antenna(this, 0F))
        for (i in 1..8) {
            antennas.add(Antenna(this, (i * 9).toFloat()))
            antennas.add(Antenna(this, (-i * 9).toFloat()))
        }
    }
}
