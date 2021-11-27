package br.com.phs.dbbrowser.ui.utils

import java.awt.Component
import java.awt.Dimension
import javax.swing.Box

fun boxSpace(w: Int = 10, h: Int = 10): Component {
    return Box.createRigidArea(Dimension(w, h))
}