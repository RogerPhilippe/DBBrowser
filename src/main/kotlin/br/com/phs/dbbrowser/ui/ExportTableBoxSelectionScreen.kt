package br.com.phs.dbbrowser.ui

import br.com.phs.dbbrowser.ui.utils.MainJFrame
import br.com.phs.dbbrowser.ui.utils.ScreenUtils
import br.com.phs.dbbrowser.ui.utils.handMouseClickListener
import br.com.phs.dbbrowser.utils.RowNumberTable
import br.com.phs.dbcore.ConnectDB
import java.awt.Container
import java.awt.FlowLayout
import java.io.BufferedWriter
import java.io.FileWriter
import javax.swing.*


class ExportTableBoxSelectionScreen(private val parent: MainJFrame): JFrame() {

    private val dbPath = parent.dbPath
    private val thisWidth = 640
    private val thisHeight = 480
    private val btnPanelHeight = 40
    private val btnPanelBottomAdjust = 4
    private lateinit var table: JTable

    init {

        this.parent.blockModalScreen()
        ScreenUtils.devScreenMode = false
        this.createdUI()

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
        // **** Buttons Panel
        val btnPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val btnPanelY = thisHeight - (btnPanelHeight + btnPanelBottomAdjust)
        btnPanel.setBounds(2, btnPanelY, (thisWidth - btnPanelBottomAdjust), btnPanelHeight)
        btnPanel.border = ScreenUtils().getBorder()
        pnlPrincipal.add(btnPanel)
        // Cancel Btn
        val cancelBtn = JButton("Cancelar")
        cancelBtn.addMouseListener(handMouseClickListener(
            released = { this.dispose() },
            parent = this
        ))
        btnPanel.add(cancelBtn)
        // OK Btn
        val okBtn = JButton("OK")
        okBtn.addMouseListener(handMouseClickListener(
            released = { this.okBtnEventClick() },
            parent = this
        ))
        btnPanel.add(okBtn)

        fillTable(pnlPrincipal)

        contentPane.add(pnlPrincipal)
    }

    private fun fillTable(panel: JPanel) {

        val tablesName = ConnectDB.getTablesName(dbPath).toTypedArray()
        val list = mutableListOf<Array<Any>>()
        tablesName.forEach {
            list.add(arrayOf(it))
        }

        table = object : JTable(list.toTypedArray(), arrayOf("Table")) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        val rowTable = RowNumberTable(table)
        val tableResultScrollPane = JScrollPane(
            table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        tableResultScrollPane.setRowHeaderView(rowTable)
        tableResultScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.tableHeader)
        tableResultScrollPane.setBounds(10, 10, panel.width-20, panel.height-70)
        panel.add(tableResultScrollPane)

    }

    private fun okBtnEventClick() {
        val tables = table.selectedRows.map {
            table.model.getValueAt(it, 0).toString()
        }
        if (tables.isNotEmpty()) {
            val file = JFileChooser()
            file.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (file.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                val selected = file.selectedFile.absolutePath
                this.generateTablesContent(selected, tables)
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Selecione ao menos uma tabela para ser exportada!",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            )
        }
    }

    private fun generateTablesContent(pathToSave: String, tables: List<String>) {

        tables.forEach { table ->

            val fileName = "$table.sql"
            val resultObject = ConnectDB.executeQuery(dbPath, "SELECT * FROM $table")
            val writer = BufferedWriter(FileWriter("$pathToSave\\$fileName"))

            val columnStr = resultObject.columns.map {
                it
            }.toString().replace("[", "(").replace("]", ")")
            val queryCreateTable = "CREATE TABLE $table $columnStr;\n"
            writer.write(queryCreateTable)
            resultObject.result.forEach { data ->
                val dataToInsert = data.map {
                    "'$it'"
                }.toString().replace("[", "(").replace("]", ")")
                val queryInsertData = "INSERT INTO $table VALUES $dataToInsert;\n"
                writer.write(queryInsertData)
            }
            writer.close()

        }

        this.dispose()

    }

}