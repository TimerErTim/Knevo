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
import java.awt.Graphics2D
import java.awt.image.BufferedImage

class Antenna(val carLocation: CarLocation, val angle: Float) {
    val previewLength: Double
        get() = 200.0

    fun getEndX(background: BufferedImage): Double {
        val startX = carLocation.x
        val totalAngle = (carLocation.angle + angle).toDouble()
        val dx = getFreeDistance(background) * Math.cos(Math.toRadians(totalAngle))
        return startX + dx
    }

    fun getEndY(background: BufferedImage): Double {
        val startY = carLocation.y
        val totalAngle = (carLocation.angle + angle).toDouble()
        val dy = getFreeDistance(background) * Math.sin(Math.toRadians(totalAngle))
        return startY + dy
    }

    fun getFreeDistance(background: BufferedImage): Double {
        val totalAngle = (carLocation.angle + angle).toDouble()
        var startX = carLocation.x
        var startY = carLocation.y
        val dx = STEP_SIZE * Math.cos(Math.toRadians(totalAngle))
        val dy = STEP_SIZE * Math.sin(Math.toRadians(totalAngle))
        while (true) {
            startX += dx
            startY += dy
            if (background.getRGB(startX.toInt(), startY.toInt()) != ROAD_COLOR.rgb && background.getRGB(
                    startX.toInt(),
                    startY.toInt()
                ) != Color.RED.rgb
            ) {
                // we're off the road
                val distX = carLocation.x - startX
                val distY = carLocation.y - startY
                return Math.sqrt(distX * distX + distY * distY)
            }
        }
    }

    fun draw(background: BufferedImage, g2d: Graphics2D) {
        g2d.color = Color.BLUE
        g2d.drawLine(
            carLocation.x.toInt(),
            carLocation.y.toInt(),
            getEndX(background).toInt(),
            getEndY(background).toInt()
        )
    }

    companion object {
        private const val STEP_SIZE = 3.0

        @JvmField
        val ROAD_COLOR = Color(255, 174, 0)
    }
}