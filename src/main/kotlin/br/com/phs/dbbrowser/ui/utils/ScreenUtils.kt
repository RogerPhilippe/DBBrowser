package br.com.phs.dbbrowser.ui.utils

import br.com.phs.dbbrowser.utils.getScaledImage
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.border.Border
import javax.swing.border.EmptyBorder

class ScreenUtils {

    companion object {
        var mainScreenWidth = 400.0
        var mainScreenHeight = 300.0
        var devMode = false
        var devScreenMode = false
    }

    fun getBorder(): Border? {
        return if (devScreenMode) {
            BorderFactory.createLineBorder(Color.BLACK)
        } else {
            EmptyBorder(0, 5, 0, 5)
        }
    }

    fun tlrBorder(): Border? {
        return BorderFactory.createMatteBorder(1, 1, 0, 1, Color.BLACK)
    }

    fun tlrbBorder(color: Color = Color.BLACK): Border? {
        return BorderFactory.createLineBorder(color)
    }

    fun getIconScaled(iconName: String, h: Int = 32, w: Int = 32) : ImageIcon {
        val btnImg = ImageIcon(javaClass.classLoader.getResource(iconName)).image
        return ImageIcon(getScaledImage(btnImg, h, w))
    }

}