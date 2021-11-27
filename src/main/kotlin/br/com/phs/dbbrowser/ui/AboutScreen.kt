package br.com.phs.dbbrowser.ui

import br.com.phs.dbbrowser.ui.utils.MainJFrame
import br.com.phs.dbbrowser.ui.utils.ScreenUtils
import br.com.phs.dbbrowser.ui.utils.boxSpace
import br.com.phs.dbbrowser.ui.utils.handMouseClickListener
import java.awt.Container
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*

class AboutScreen(private val parent: MainJFrame): JFrame() {

    private val thisWidth = 600
    private val thisHeight = 300
    private val btnPanelHeight = 40
    private val btnPanelBottomAdjust = 4

    init {

        this.parent.blockModalScreen()
        createdUI()

    }

    override fun dispose() {
        super.dispose()
        this.parent.releaseModalScreen()
    }

    private fun createdUI() {

        defaultCloseOperation = DISPOSE_ON_CLOSE
        setSize(thisWidth,thisHeight)
        setLocationRelativeTo(parent)
        isUndecorated = true

        addComponents(this.contentPane)

    }

    private fun addComponents(contentPane: Container) {
        // **** MAIN PANEL ****
        val pnlPrincipal = JPanel()
        pnlPrincipal.setBounds(0, 0, thisWidth, thisHeight)
        pnlPrincipal.layout = null
        pnlPrincipal.border = ScreenUtils().tlrbBorder()
        // Content Panel
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.setBounds(10,10, thisWidth-20, thisHeight-20-44)
        contentPanel.border = ScreenUtils().getBorder()
        pnlPrincipal.add(contentPanel)
        val iconApp = JLabel(ScreenUtils().getIconScaled("dbbrowser_icon.png", 86, 86))
        val appNameAndVersion = JLabel("DBBRowser versão 1.0.0-betav2 - 2021")
        appNameAndVersion.font = Font("Serif", Font.PLAIN, 17)
        val authorAndLocation = JLabel("PhilippeSis - Roger Philippe - Santo André - SP - Brasil")
        authorAndLocation.font = Font("Serif", Font.PLAIN, 17)
        val aboutApplication = JLabel("Essa é uma aplicação gratuida para gerenciamente de bancos de dados.")
        aboutApplication.font = Font("Serif", Font.PLAIN, 17)
        contentPanel.add(iconApp)
        contentPanel.add(boxSpace())
        contentPanel.add(appNameAndVersion)
        contentPanel.add(boxSpace(h = 20))
        contentPanel.add(authorAndLocation)
        contentPanel.add(aboutApplication)
        // **** Buttons Panel
        val btnPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val btnPanelY = thisHeight - (btnPanelHeight + btnPanelBottomAdjust)
        btnPanel.setBounds(2, btnPanelY, (thisWidth - btnPanelBottomAdjust), btnPanelHeight)
        btnPanel.border = ScreenUtils().getBorder()
        pnlPrincipal.add(btnPanel)
        // Cancel Btn
        val cancelBtn = JButton("Fechar")
        cancelBtn.addMouseListener(handMouseClickListener(
            released = { this.dispose() },
            parent = this
        ))
        btnPanel.add(cancelBtn)

        contentPane.add(pnlPrincipal)
    }

}