package eu.timerertim.knevo.shared.cppn

import eu.timerertim.knevo.Genome
import java.awt.Color
import java.awt.image.BufferedImage

class CPPN(private val genome: Genome) {
    fun generateRGB(scale: Float = 1F): BufferedImage {
        val image = BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB)

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val (r, g, b) =
                    genome(floatArrayOf(scale * x / image.width.toFloat(), scale * y / image.height.toFloat()))
                image.setRGB(x, y, Color(r, g, b).rgb)
            }
        }

        return image
    }

    fun generateGray(scale: Float = 1F): BufferedImage {
        val image = BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB)

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val (gray) =
                    genome(floatArrayOf(scale * x / image.width.toFloat(), scale * y / image.height.toFloat()))
                image.setRGB(x, y, Color(gray, gray, gray).rgb)
            }
        }

        return image
    }
}
