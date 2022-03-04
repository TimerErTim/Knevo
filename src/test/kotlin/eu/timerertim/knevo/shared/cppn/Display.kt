package eu.timerertim.knevo.shared.cppn

import java.awt.FlowLayout
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

object Display {
    val frame = JFrame()
    val label = JLabel()

    init {
        frame.contentPane.layout = FlowLayout()
        frame.contentPane.add(label)
        frame.isVisible = true
    }
}

fun display(image: BufferedImage) {
    Display.label.icon = ImageIcon(image)
    Display.frame.pack()
}
