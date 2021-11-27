package br.com.phs.dbbrowser.ui.utils

import javax.swing.JFrame

abstract class MainJFrame: JFrame() {

    var dbPath = ""

    abstract fun blockModalScreen()
    abstract fun releaseModalScreen()

}