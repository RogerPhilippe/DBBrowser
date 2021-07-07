package br.com.phs.dbbrowser.ui

import br.com.phs.dbbrowser.ui.utils.ScreenUtils
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.devScreenMode
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.mainScreenHeight
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.mainScreenWidth
import br.com.phs.dbbrowser.utils.*
import br.com.phs.dbcore.ConnectDB
import br.com.phs.dbcore.ResultObject
import br.com.phs.dbcore.ResultTypeEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.event.TableModelListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.DefaultCaret
import javax.swing.text.Document
import javax.swing.undo.CannotUndoException
import javax.swing.undo.UndoManager
import kotlin.system.exitProcess


class MainScreen: JFrame() {

    private val appName = "DBBrowser"
    private var mouseDownCompCoords: Point? = null
    private val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    private var resultObject: ResultObject? = null
    private val fileChooser = JFileChooser()
    private val filterDQLiteDB = FileNameExtensionFilter("DB file", "db")
    private var dbPath = ""
    private var undoManager = UndoManager()
    private lateinit var document: Document
    private lateinit var inputMap: InputMap
    private lateinit var actionMap: ActionMap

    // **** Components ****
    private val title = JLabel(appName)
    private val terminalTextArea = JTextArea()
    private val sqlTextArea = JTextArea()
    private val tablePanel = JPanel()
    private val terminalPanel = JPanel()
    private var openDbBtn: JLabel? = null
    private var executeBtn: JLabel? = null
    private var executeSelectionBtn: JLabel? = null
    private var execUpdateDbBtn: JLabel? = null
    private val menuBar = JMenuBar()
    private lateinit var fileMenu: JMenu

    init {
        mainScreenWidth = gd.displayMode.width * .70
        mainScreenHeight = gd.displayMode.height * .90
        devScreenMode = false
        createUI()
    }

    private fun createUI() {

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(mainScreenWidth.toInt(), mainScreenHeight.toInt())
        setLocationRelativeTo(null)
        isUndecorated = true

        addMouseMotionListener(getMouseMotionListener(this))

        addComponents(this.contentPane)

    }

    private fun addComponents(pane: Container) {

        // **** MAIN PANEL ****
        val pnlPrincipal = JPanel()
        pnlPrincipal.setBounds(0, 0, mainScreenWidth.toInt(), mainScreenHeight.toInt())
        pnlPrincipal.layout = null
        pnlPrincipal.border = ScreenUtils().tlrbBorder()

        // **** TITLE PANEL BAR ****
        val titleBarPanel = JPanel()
        titleBarPanel.setBounds(0, 0, mainScreenWidth.toInt(), 32)
        titleBarPanel.layout = null
        titleBarPanel.border = ScreenUtils().tlrBorder()
        pnlPrincipal.add(titleBarPanel)
        titleBarPanel.addMouseListener(handMouseClickListener(
            {
                cursor = Cursor(Cursor.HAND_CURSOR)
                mouseDownCompCoords = null
            },
            {
                cursor = Cursor(Cursor.MOVE_CURSOR)
                mouseDownCompCoords = it?.point
            }))
        titleBarPanel.addMouseMotionListener(getMouseMotionListenerTitleBar())

        // **** TITLE PANEL BAR ICON ****
        val iconAppH = 20
        val iconAppW = 20
        val iconApp = JLabel(getIconScaled("dbbrowser_icon.png", iconAppH, iconAppW))
        val iconAppY = (titleBarPanel.height/2) - (iconAppH/2)
        iconApp.setBounds(10, iconAppY, iconAppW, iconAppH)
        titleBarPanel.add(iconApp)

        // **** TITLE ****
        title.setBounds((iconApp.x+iconApp.width)+10, 0, titleBarPanel.width-32, titleBarPanel.height)
        titleBarPanel.add(title)

        // **** CLOSE BTN ****
        val img = ImageIcon(javaClass.classLoader.getResource("close.png")).image
        val closeBtn = JLabel(ImageIcon(getScaledImage(img, 20, 20)))
        closeBtn.setBounds(mainScreenWidth.toInt()-32, 0, 32, 32)
        closeBtn.horizontalAlignment = SwingConstants.CENTER
        closeBtn.verticalAlignment = SwingConstants.CENTER
        closeBtn.addMouseListener(handMouseClickListener({
            exitProcess(0)
        }))
        closeBtn.border = ScreenUtils().getBorder()
        titleBarPanel.add(closeBtn)

        // **** MINIMIZE BTN ****
        val miniImg = ImageIcon(javaClass.classLoader.getResource("minimize_icon.png")).image
        val miniBtn = JLabel(ImageIcon(getScaledImage(miniImg, 20, 20)))
        miniBtn.setBounds(closeBtn.x-26, 0, 32, 32)
        miniBtn.horizontalAlignment = SwingConstants.CENTER
        miniBtn.verticalAlignment = SwingConstants.CENTER
        miniBtn.addMouseListener(handMouseClickListener({
            state = Frame.ICONIFIED
        }))
        miniBtn.border = ScreenUtils().getBorder()
        titleBarPanel.add(miniBtn)

        // **** MENU BAR ****
        fileMenu = JMenu(getLabel(0, FILE))
        val editMenu = JMenu(getLabel(0, EDIT))
        val settingsMenu = JMenu(getLabel(0, SETTINGS))
        val aboutMenu = JMenu(getLabel(0, ABOUT))
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(settingsMenu)
        menuBar.add(aboutMenu)
        val newAction = JMenuItem("New")
        val openAction = JMenuItem("Open")
        val exitAction = JMenuItem(getLabel(0, EXIT))
        exitAction.addActionListener {
            exitProcess(0)
        }
        val cutAction = JMenuItem("Cut")
        val copyAction = JMenuItem("Copy")
        val pasteAction = JMenuItem("Paste")
        fileMenu.add(newAction)
        fileMenu.add(openAction)
        fileMenu.addSeparator()
        fileMenu.add(exitAction)
        editMenu.add(cutAction)
        editMenu.add(copyAction)
        editMenu.add(pasteAction)
        menuBar.setBounds(1,titleBarPanel.height, mainScreenWidth.toInt()-2, 24)
        pnlPrincipal.add(menuBar)

        // **** BTNs PANEL ****
        val pnlBtn = JPanel()
        val yTmp = menuBar.y + menuBar.height
        pnlBtn.setBounds(1, yTmp, mainScreenWidth.toInt()-2, 70)
        pnlBtn.layout = null
        pnlBtn.border = ScreenUtils().getBorder()
        pnlPrincipal.add(pnlBtn)
        // **** BTNs PANEL OPEN DB ****
        openDbBtn = makeBtn("open_database_icon.png")
        if (openDbBtn != null) {
            openDbBtn!!.setBounds(10, ((pnlBtn.height/2)-23), 46, 46)
            pnlBtn.add(openDbBtn)
        }
        // **** BTNs PANEL PLAY ****
        executeBtn = makeBtn("play_icon_512_disable.png")
        if (executeBtn != null) {
            val executeBtnX = (openDbBtn?.width?: 0)+20
            executeBtn!!.setBounds(executeBtnX, ((pnlBtn.height/2)-23), 46, 46)
            pnlBtn.add(executeBtn)
        }
        // **** BTNs PANEL EXECUTE SELECTION ****
        executeSelectionBtn = makeBtn("execute_selection_disable.png")
        if (executeSelectionBtn != null) {
            val executeSelectionBtnX = ((executeBtn?.x?: 0) + (executeBtn?.width?: 0))+10
            executeSelectionBtn!!.setBounds(executeSelectionBtnX, ((pnlBtn.height/2)-23), 46, 46)
            pnlBtn.add(executeSelectionBtn)
        }
        // **** BTNs PANEL EXECUTE UPDATE ****
        execUpdateDbBtn = makeBtn("thunder_icon_512_disable.png")
        if (execUpdateDbBtn != null) {
            val execUpdateDbBtnX = ((executeSelectionBtn?.x?: 0) + (executeSelectionBtn?.width?: 0))+10
            execUpdateDbBtn!!.setBounds(execUpdateDbBtnX, ((pnlBtn.height/2)-23), 46, 46)
            pnlBtn.add(execUpdateDbBtn)
        }

        // **** SQL Panel ****
        val sqlPanel = JPanel()
        val yTmp2 = pnlBtn.y + pnlBtn.height
        sqlPanel.setBounds(10, yTmp2, mainScreenWidth.toInt()-20, 300)
        sqlPanel.border = BorderFactory.createTitledBorder(getLabel(0, SQL_CMD_LABEL))
        sqlPanel.layout = null
        pnlPrincipal.add(sqlPanel)
        // **** SQL TEXT AREA ****
        sqlTextArea.font = sqlTextArea.font.deriveFont(14f)
        sqlTextArea.lineWrap = true
        document = sqlTextArea.document
        inputMap = sqlTextArea.getInputMap(JComponent.WHEN_FOCUSED)
        actionMap = sqlTextArea.actionMap
        val tln = TextLineNumber(sqlTextArea)
        val sqlTextAreaScrollPane = JScrollPane(sqlTextArea)
        sqlTextAreaScrollPane.setRowHeaderView(tln)
        sqlTextAreaScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        sqlTextAreaScrollPane.setBounds(10, 16, sqlPanel.width-20, sqlPanel.height-26)
        sqlPanel.add(sqlTextAreaScrollPane)

        // **** TERMINAL PANEL ****
        terminalPanel.setBounds(10, (mainScreenHeight.toInt()-180), mainScreenWidth.toInt()-20, 170)
        terminalPanel.border = BorderFactory.createTitledBorder(getLabel(0, TERMINAL))
        terminalPanel.layout = null
        pnlPrincipal.add(terminalPanel)
        // **** TERMINAL TEXT AREA ****
        terminalTextArea.lineWrap = true
        terminalTextArea.isEditable = false
        val caret = (terminalTextArea.caret as DefaultCaret)
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE
        val terminalTextAreaScrollPane = JScrollPane()
        terminalTextAreaScrollPane.setViewportView(terminalTextArea)
        terminalTextAreaScrollPane.setBounds(10, 16, terminalPanel.width-20, terminalPanel.height-26)
        terminalPanel.add(terminalTextAreaScrollPane)

        // **** TABLE PANEL ****
        val yTmp3 = sqlPanel.y + sqlPanel.height
        tablePanel.setBounds(10, yTmp3+10, mainScreenWidth.toInt()-20, terminalPanel.y - (sqlPanel.y + sqlPanel.height)-20)
        tablePanel.border = BorderFactory.createTitledBorder(getLabel(0, SQL_RESULT_LABEL))
        tablePanel.layout = null
        pnlPrincipal.add(tablePanel)

        this.setupListeners()

        pane.add(pnlPrincipal)

    }

    private fun setupListeners() {

        executeBtn?.addMouseListener(handMouseClickListener({

            if (dbPath.isEmpty()) {
                addTerminalMsg("Selecione o banco de dados!")
                return@handMouseClickListener
            }

            if (sqlTextArea.text.isNotEmpty()) {
                val commands = sqlTextArea.text.replace("\n", "").split(";")
                for (command in commands) {
                    if (command.isEmpty()) continue
                    if (command.startsWith("--")) continue
                    execute(command)
                }
            }

        }))

        openDbBtn?.addMouseListener(handMouseClickListener({

            fileChooser.addChoosableFileFilter(filterDQLiteDB)
            val returnVal = fileChooser.showOpenDialog(this)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                dbPath = file.absolutePath
                addTerminalMsg("Arquivo selecionado: ${file.absoluteFile}")
                title.text = "$appName - ${file.absoluteFile}"
                executeBtn?.icon = getIconScaled("play_icon_512.png")
                executeSelectionBtn?.icon = getIconScaled("execute_selection.png")
            }
        }))

        executeSelectionBtn?.addMouseListener(handMouseClickListener({
            val selectedText = sqlTextArea.selectedText
            if (selectedText.isNotEmpty()) {
                execute(selectedText)
            }
        }))

        execUpdateDbBtn?.addMouseListener(handMouseClickListener({
            execUpdateDbBtn?.icon = getIconScaled("thunder_icon_512_disable.png")
        }))

        // To avoid cursor bug
        terminalPanel.addMouseListener(defaultCursorListener())
        menuBar.addMouseListener(defaultCursorListener())
        fileMenu.addMouseListener(defaultCursorListener())

        document.addUndoableEditListener {
            undoManager.addEdit(it.edit)
        }

        inputMap.put(KeyStroke.getKeyStroke(
            KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMask), "Undo")
        inputMap.put(KeyStroke.getKeyStroke(
            KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMask or InputEvent.SHIFT_MASK), "Redo")

        actionMap.put("Undo", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                try {
                    if (undoManager.canUndo())
                        undoManager.undo()
                } catch (exp: CannotUndoException) {
                    exp.printStackTrace()
                }
            }
        })
        actionMap.put("Redo", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                try {
                    if (undoManager.canRedo())
                        undoManager.redo()
                } catch (exp: CannotUndoException) {
                    exp.printStackTrace()
                }
            }

        })

    }

    private fun execute(command: String) {

        tablePanel.removeAll()
        addTerminalMsg("Executando...")

        SwingUtilities.invokeLater {

            this.resultObject = ConnectDB.executeQuery(dbPath, command)
            if (this.resultObject != null && this.resultObject?.hasError == false  &&
                this.resultObject?.columns?.isNotEmpty() == true && this.resultObject?.resultType == ResultTypeEnum.SELECT) {
                val columns: Array<String> = this.resultObject!!.columns
                val data: Array<Array<Any>> = this.resultObject!!.result
                // **** TABLE RESULT ****
                val table = JTable(data, columns)
                table.autoResizeMode = JTable.AUTO_RESIZE_OFF
                val rowTable = RowNumberTable(table)
                table.model.addTableModelListener(getTableModelListener())
                val tableResultScrollPane = JScrollPane(
                    table,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                )
                tableResultScrollPane.setRowHeaderView(rowTable)
                tableResultScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.tableHeader)
                tableResultScrollPane.setBounds(10, 16, tablePanel.width-20, tablePanel.height-26)
                tablePanel.add(tableResultScrollPane)
            } else if (this.resultObject != null && this.resultObject?.hasError == false) {
                addTerminalMsg("Comando executado.")
            }

            if (this.resultObject != null && this.resultObject?.hasError == false) {
                addTerminalMsg("Linhas retornadas ${this.resultObject?.result?.size ?: 0}")
                val args: MutableList<Any> = mutableListOf()
                args.add(getRandomHash())
                args.add(Calendar.getInstance().timeInMillis)
                args.add(command)
                ConnectDB.insertHistoric(args)
            } else if (this.resultObject != null && this.resultObject?.hasError == true) {
                addTerminalMsg("Ocorreu um erro na execução ${this.resultObject?.errorMsg}")
            }

        }

    }

    private fun defaultCursorListener(): MouseListener {
        return object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                cursor = Cursor(Cursor.DEFAULT_CURSOR)
            }
            override fun mouseExited(e: MouseEvent?) {
                cursor = Cursor(Cursor.DEFAULT_CURSOR)
            }
        }
    }

    private fun handMouseClickListener(
        released: (e: MouseEvent?)-> Unit,
        pressed: ((e: MouseEvent?) -> Unit)? = null
    ): MouseListener {
        return object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                cursor = Cursor(Cursor.HAND_CURSOR)
            }
            override fun mouseExited(e: MouseEvent?) {
                cursor = Cursor(Cursor.DEFAULT_CURSOR)
            }
            override fun mouseReleased(e: MouseEvent?) {
                released(e)
            }
            override fun mousePressed(e: MouseEvent) {
                if (pressed != null)
                    pressed(e)
            }
        }
    }

    private fun getMouseMotionListener(frame: JFrame): MouseMotionAdapter {
        return object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                super.mouseMoved(e)
                val currCoords = e.locationOnScreen

                cursor = if (currCoords.x <= frame.x +12 && currCoords.y >= (mainScreenHeight.toInt()+frame.y)-20) {
                    Cursor(Cursor.NE_RESIZE_CURSOR)
                } else if (currCoords.x <= frame.x +12) {
                    Cursor(Cursor.E_RESIZE_CURSOR)
                } else if (currCoords.x >= (mainScreenWidth.toInt()+frame.x)-12 &&
                    (currCoords.y >= (mainScreenHeight.toInt()+frame.y)-20)) {
                    Cursor(Cursor.NW_RESIZE_CURSOR)
                } else if (currCoords.x >= (mainScreenWidth.toInt()+frame.x)-12) {
                    Cursor(Cursor.W_RESIZE_CURSOR)
                } else if (currCoords.y >= (mainScreenHeight.toInt()+frame.y)-20) {
                    Cursor(Cursor.N_RESIZE_CURSOR)
                } else {
                    Cursor(Cursor.DEFAULT_CURSOR)
                }
            }
        }
    }

    private fun getMouseMotionListenerTitleBar(): MouseMotionAdapter {
        return object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val currCoords = e.locationOnScreen
                setLocation(currCoords.x - (mouseDownCompCoords?.x?: 0), currCoords.y - (mouseDownCompCoords?.y?: 0))
            }

            override fun mouseMoved(e: MouseEvent) {
                super.mouseMoved(e)
                val currCoords = e.locationOnScreen
            }
        }
    }

    private fun addTerminalMsg(msg: String) = runBlocking {
        withContext(Dispatchers.IO) {
            terminalTextArea.append("${Date().toStringFormatted()} - $msg\n")
        }
    }

    private fun makeBtn(iconName: String): JLabel {
        val playBtnImg = getIconScaled(iconName).image
        val btn = JLabel(ImageIcon(getScaledImage(playBtnImg, 32, 32)))
        btn.horizontalAlignment = SwingConstants.CENTER
        btn.verticalAlignment = SwingConstants.CENTER
        btn.border = ScreenUtils().tlrbBorder(Color.GRAY)
        return  btn
    }

    private fun getIconScaled(iconName: String, h: Int = 32, w: Int = 32) : ImageIcon {
        val btnImg = ImageIcon(javaClass.classLoader.getResource(iconName)).image
        return ImageIcon(getScaledImage(btnImg, h, w))
    }

    private fun getTableModelListener(): TableModelListener {

        return TableModelListener {
            execUpdateDbBtn?.icon = getIconScaled("thunder_icon_512.png")
        }
    }

}