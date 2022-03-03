package br.com.phs.dbbrowser.ui

import br.com.phs.dbbrowser.data.enums.SideMousePosition
import br.com.phs.dbbrowser.services.Server
import br.com.phs.dbbrowser.ui.utils.MainJFrame
import br.com.phs.dbbrowser.ui.utils.ScreenUtils
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.devScreenMode
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.mainScreenHeight
import br.com.phs.dbbrowser.ui.utils.ScreenUtils.Companion.mainScreenWidth
import br.com.phs.dbbrowser.utils.*
import br.com.phs.dbcore.ConnectDB
import br.com.phs.dbcore.ResultObject
import br.com.phs.dbcore.ResultTypeEnum
import com.Ostermiller.Syntax.HighlightedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.stream.Collectors
import javax.swing.*
import javax.swing.event.TableModelListener
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultCaret
import javax.swing.undo.CannotUndoException
import javax.swing.undo.UndoManager
import kotlin.system.exitProcess


class MainScreen: MainJFrame() {

    private val version = "1.0.0-alphav1"
    private val appName = "DBBrowser"
    private val applicationConfig = getApplicationConfig()
    private var mouseDownCompCoords: Point? = null
    private val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    private val fileChooser = JFileChooser()
    private val filterDQLiteDB = FileNameExtensionFilter("DB file", "db")
    private var undoManager = UndoManager()
    private lateinit var inputMap: InputMap
    private lateinit var actionMap: ActionMap
    private var suggestion: SuggestionPanel? = null
    private val keyWords = mutableListOf<String>()
    private val hDocument = HighlightedDocument()
    private var blockByModalScreen = false
    private var thereIsSelectedDB = false
    private var mousePosition = SideMousePosition.NONE
    private var sqliteRealTimeConnected = false

    // **** Components ****
    private lateinit var pnlPrincipal: JPanel
    private lateinit var titleBarPanel: JPanel
    private val title = JLabel(appName)
    private lateinit var closeBtn: JLabel
    private lateinit var miniBtn: JLabel
    private lateinit var pnlBtn: JPanel
    private lateinit var sqlPanel: JPanel
    private lateinit var sqlTextAreaScrollPane: JScrollPane
    private val terminalTextArea = JTextArea()
    private lateinit var terminalTextAreaScrollPane: JScrollPane
    private val sqlTextPane = JTextPane(hDocument)
    private val tablePanel = JPanel()
    private var tableResultScrollPane: JScrollPane? = null
    private val terminalPanel = JPanel()
    private lateinit var openDbBtn: JLabel
    private lateinit var executeBtn: JLabel
    private lateinit var execUpdateDbBtn: JLabel
    private val menuBar = JMenuBar()
    private lateinit var fileMenu: JMenu
    private lateinit var openAction: JMenuItem
    private val connectionStatus = JLabel("Estado: Parado")
    private val connectionPort = JTextField("4500")
    private val connectBtn = JButton("Conectar")

    private val minimumScreenWidth: Double?
    private val minimumScreenHeight: Double?

    private val server = Server()

    init {

        println("DBBrowser start...")
        println("Version: $version")

        mainScreenWidth = gd.displayMode.width * .70
        minimumScreenWidth = gd.displayMode.width * .40
        mainScreenHeight = gd.displayMode.height * .90
        minimumScreenHeight = gd.displayMode.height * .70
        devScreenMode = false
        hDocument.setHighlightStyle(HighlightedDocument.SQL_STYLE)
        val recovered = readLastContent()
        sqlTextPane.text = recovered
        sqlTextPane.isEditable = false

        createUI()

    }

    private fun createUI() {

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(mainScreenWidth.toInt(), mainScreenHeight.toInt())
        setLocationRelativeTo(null)
        isUndecorated = true

        addMouseMotionListener(getMouseMotionListener(this))
        addMouseListener(handMouseClickListener(released = {
            mousePosition = SideMousePosition.NONE
        }))

        addComponents(this.contentPane)

    }

    private fun addComponents(contentPane: Container) {

        // **** MAIN PANEL ****
        pnlPrincipal = JPanel()
        pnlPrincipal.setBounds(0, 0, mainScreenWidth.toInt(), mainScreenHeight.toInt())
        pnlPrincipal.layout = null
        pnlPrincipal.border = ScreenUtils().tlrbBorder()

        // **** TITLE PANEL BAR ****
        titleBarPanel = JPanel()
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
        val iconApp = JLabel(ScreenUtils().getIconScaled("dbbrowser_icon.png", iconAppH, iconAppW))
        val iconAppY = (titleBarPanel.height/2) - (iconAppH/2)
        iconApp.setBounds(10, iconAppY, iconAppW, iconAppH)
        titleBarPanel.add(iconApp)

        // **** TITLE ****
        title.setBounds((iconApp.x+iconApp.width)+10, 0, titleBarPanel.width-32, titleBarPanel.height)
        titleBarPanel.add(title)

        // **** CLOSE BTN ****
        val img = ImageIcon(javaClass.classLoader.getResource("close.png")).image
        closeBtn = JLabel(ImageIcon(getScaledImage(img, 20, 20)))
        closeBtn.setBounds(mainScreenWidth.toInt()-32, 0, 32, 32)
        closeBtn.horizontalAlignment = SwingConstants.CENTER
        closeBtn.verticalAlignment = SwingConstants.CENTER
        closeBtn.addMouseListener(handMouseClickListener({
            closeApplication()
        }))
        closeBtn.border = ScreenUtils().getBorder()
        titleBarPanel.add(closeBtn)

        // **** MINIMIZE BTN ****
        val miniImg = ImageIcon(javaClass.classLoader.getResource("minimize_icon.png")).image
        miniBtn = JLabel(ImageIcon(getScaledImage(miniImg, 20, 20)))
        miniBtn.setBounds(closeBtn.x-26, 0, 32, 32)
        miniBtn.horizontalAlignment = SwingConstants.CENTER
        miniBtn.verticalAlignment = SwingConstants.CENTER
        miniBtn.addMouseListener(handMouseClickListener({
            if (!blockByModalScreen) {
                state = Frame.ICONIFIED
            }
        }))
        miniBtn.border = ScreenUtils().getBorder()
        titleBarPanel.add(miniBtn)

        // **** MENU BAR ****
        fileMenu = JMenu(getLabel(0, FILE))
        val editMenu = JMenu(getLabel(0, EDIT))
        val settingsMenu = JMenu(getLabel(0, SETTINGS))
        val aboutMenu = JMenu("Ajuda")
        val actionAbout = JMenuItem("Sobre DBBrowser")
        actionAbout.addActionListener {
            this.createAndOpenAboutBox()
        }
        menuBar.add(fileMenu)
        menuBar.add(editMenu)
        menuBar.add(settingsMenu)
        menuBar.add(aboutMenu)
        val newAction = JMenuItem(getLabel(0, NEW))
        openAction = JMenuItem(getLabel(0, OPEN_DB))
        val exportDB = JMenuItem(getLabel(0, EXPORT_DATABASE))
        exportDB.addActionListener {
            if (thereIsSelectedDB) {
                this.createAndOpenExportTablesBoxSelection()
            }
        }
        val exitAction = JMenuItem(getLabel(0, EXIT))
        exitAction.addActionListener {
            closeApplication()
        }
        val cutAction = JMenuItem("Cut")
        val copyAction = JMenuItem("Copy")
        val pasteAction = JMenuItem("Paste")
        fileMenu.add(newAction)
        fileMenu.add(openAction)
        fileMenu.add(exportDB)
        fileMenu.addSeparator()
        fileMenu.add(exitAction)
        editMenu.add(cutAction)
        editMenu.add(copyAction)
        editMenu.add(pasteAction)
        aboutMenu.add(actionAbout)
        menuBar.setBounds(1,titleBarPanel.height, mainScreenWidth.toInt()-2, 24)
        pnlPrincipal.add(menuBar)

        // **** BTNs PANEL ****
        pnlBtn = JPanel()
        val yTmp = menuBar.y + menuBar.height
        pnlBtn.setBounds(1, yTmp, mainScreenWidth.toInt()-2, 70)
        pnlBtn.layout = null
        pnlBtn.border = ScreenUtils().getBorder()
        pnlPrincipal.add(pnlBtn)
        // **** BTNs PANEL OPEN DB ****
        openDbBtn = makeBtn("open_database_icon.png")
        openDbBtn.toolTipText = "Selecionar banco de dados"
        openDbBtn.setBounds(10, ((pnlBtn.height/2)-23), 46, 46)
        pnlBtn.add(openDbBtn)
        // **** BTNs PANEL EXECUTE COMMAND ****
        executeBtn = makeBtn("play_icon_512_disable.png")
        executeBtn.toolTipText = "Executar / Executar Seleção"
        val executeBtnX = openDbBtn.width +20
        executeBtn.setBounds(executeBtnX, ((pnlBtn.height/2)-23), 46, 46)
        pnlBtn.add(executeBtn)
        // **** BTNs PANEL EXECUTE UPDATE ****
        execUpdateDbBtn = makeBtn("thunder_icon_512_disable.png")
        execUpdateDbBtn.toolTipText = "Executar Alt. Tabela"
        val execUpdateDbBtnX = (executeBtn.x + executeBtn.width)+10
        execUpdateDbBtn.setBounds(execUpdateDbBtnX, ((pnlBtn.height/2)-23), 46, 46)
        pnlBtn.add(execUpdateDbBtn)
        // Panel SQLite Realtime
        val jSQLiteRealTimePanel = JPanel()
        val jSQLiteRealTimePanelX = execUpdateDbBtn.x + execUpdateDbBtn.width + 10
        jSQLiteRealTimePanel.layout = null
        jSQLiteRealTimePanel.setBounds(jSQLiteRealTimePanelX, 5, 320, pnlBtn.height - 10)
        val border = BorderFactory.createTitledBorder("SQLite RealTime")
        border.titleFont = Font("SansSerif", Font.PLAIN, 11)
        jSQLiteRealTimePanel.border = border
        pnlBtn.add(jSQLiteRealTimePanel)
        // Port Text Field
        connectionPort.font = Font("SansSerif", Font.BOLD, 18)
        connectionPort.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        connectionPort.setBounds(10, (jSQLiteRealTimePanel.height / 2), 65, 24)
        jSQLiteRealTimePanel.add(connectionPort)
        // Port Label
        val portLabel = JLabel("Porta")
        portLabel.font = Font("SansSerif", Font.BOLD, 11)
        val portLabelX = connectionPort.x
        val portLabelY = connectionPort.y - 18
        portLabel.setBounds(portLabelX, portLabelY, 60, 20)
        jSQLiteRealTimePanel.add(portLabel)
        // SQLite Realtime Connect Btn
        val connectBtnX = connectionPort.x + connectionPort.width + 10
        connectBtn.setBounds(connectBtnX, (jSQLiteRealTimePanel.height / 2), 90, 24)
        jSQLiteRealTimePanel.add(connectBtn)
        // Connection Status Label
        val connectionStatusX = connectBtn.x + connectBtn.width + 10
        connectionStatus.setBounds(connectionStatusX, (jSQLiteRealTimePanel.height / 2), 120, 20)
        jSQLiteRealTimePanel.add(connectionStatus)

        // **** SQL Panel ****
        sqlPanel = JPanel()
        val yTmp2 = pnlBtn.y + pnlBtn.height
        sqlPanel.setBounds(10, yTmp2, mainScreenWidth.toInt()-20, (mainScreenHeight*.3).toInt())
        sqlPanel.border = BorderFactory.createTitledBorder(getLabel(0, SQL_CMD_LABEL))
        sqlPanel.layout = null
        pnlPrincipal.add(sqlPanel)
        // **** SQL TEXT AREA ****
        inputMap = sqlTextPane.getInputMap(JComponent.WHEN_FOCUSED)
        actionMap = sqlTextPane.actionMap
        val tln = TextLineNumber(sqlTextPane)
        sqlTextAreaScrollPane = JScrollPane(sqlTextPane)
        sqlTextAreaScrollPane.setRowHeaderView(tln)
        sqlTextAreaScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        sqlTextAreaScrollPane.setBounds(10, 16, sqlPanel.width-20, sqlPanel.height-26)
        sqlPanel.add(sqlTextAreaScrollPane)

        // **** TERMINAL PANEL ****
        terminalPanel.setBounds(10, (mainScreenHeight.toInt()-180), mainScreenWidth.toInt()-20, (mainScreenHeight*.18).toInt())
        terminalPanel.border = BorderFactory.createTitledBorder(getLabel(0, TERMINAL))
        terminalPanel.layout = null
        pnlPrincipal.add(terminalPanel)
        // **** TERMINAL TEXT AREA ****
        terminalTextArea.lineWrap = true
        terminalTextArea.isEditable = false
        val caret = (terminalTextArea.caret as DefaultCaret)
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE
        terminalTextAreaScrollPane = JScrollPane()
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

        contentPane.add(pnlPrincipal)

    }

    private fun setupListeners() {

        executeBtn.addMouseListener(handMouseClickListener({

            if (!sqliteRealTimeConnected && dbPath.isEmpty()) {
                addTerminalMsg("Selecione o banco de dados!")
                return@handMouseClickListener
            }

            if (sqlTextPane.text.isNotEmpty()) {

                val selectedText = sqlTextPane.selectedText?: ""
                val text = selectedText.ifEmpty { sqlTextPane.text }

                val commandStr = StringBuilder()

                text.split("\n").forEach { line ->
                    if (line.isNotEmpty() && !line.startsWith("--"))
                        commandStr.append(line)
                }

                if (commandStr.isEmpty())
                    return@handMouseClickListener

                if (sqliteRealTimeConnected) {

                    server.sendCommand(commandStr.toString()) {

                        if (it?.result != null) {

                            if (it.status) {

                                val columns = it.result.get("columns") as JSONArray
                                val content = it.result.get("content") as JSONArray

                                val lines = mutableListOf<Array<Any>>()
                                content.map { value -> (value as JSONObject) }.forEach { item ->

                                    val lineColumns = mutableListOf<String>()
                                    columns.forEach { column ->
                                        lineColumns.add(item.get(column.toString()) as String)
                                    }

                                    lines.add(lineColumns.toTypedArray())

                                }

                                ResultObject().apply {

                                    // Object attributes
                                    this.columns = columns.map { line -> line.toString() }.toTypedArray()
                                    this.result = lines.toTypedArray()

                                    tablePanel.removeAll()
                                    fillGrid(commandStr.toString(), this)
                                }

                            } else {
                                addTerminalMsg("Erro: ${it.result["status"]} - Content: ${it.result.get("content")}")
                            }

                        }

                    }

                } else {

                    val commands = commandStr.toString().split(";")

                    addTerminalMsg("Executando...")

                    for (command in commands) {
                        execute(command)
                    }

                }

            }

        }))

        connectBtn.addMouseListener(handMouseClickListener({

            if (sqliteRealTimeConnected) {

                server.close()
                connectBtn.text = "Conectar"
                sqlTextPane.isEditable = false
                sqliteRealTimeConnected = false
                executeBtn.icon = ScreenUtils().getIconScaled("play_icon_512_disable.png")
                connectionStatus.text = "Estado: Parado"

            } else {

                if (server.connecting) {
                    server.close()
                    connectBtn.text = "Conectar"
                    sqlTextPane.isEditable = false
                    sqliteRealTimeConnected = false
                    executeBtn.icon = ScreenUtils().getIconScaled("play_icon_512_disable.png")
                    connectionStatus.text = "Estado: Parado"
                    return@handMouseClickListener
                }

                connectionStatus.text = "Estado: Esperando"
                connectBtn.text = "Cancelar"
                var port = 4500
                val connectionPortText = connectionPort.text
                if (!connectionPortText.isNullOrEmpty()) {
                    port = Integer.parseInt(connectionPortText)
                }

                object : SwingWorker<Boolean, Void>() {

                    override fun doInBackground(): Boolean {

                        var result = false

                        server.initServer(port = port) {
                            connectionStatus.text = if (it) "Estado: Conectado" else "Estado: Parado"

                            if (it) {
                                connectBtn.text = "Desconectar"
                                executeBtn.icon = ScreenUtils().getIconScaled("play_icon_512.png")
                                sqlTextPane.isEditable = true
                                sqliteRealTimeConnected = true
                            }

                            result = it

                        }

                        return result

                    }

                }.execute()

            }

        }))

        openDbBtn.addMouseListener(handMouseClickListener({ openBD() }))
        openAction.addMouseListener(handMouseClickListener({ openBD() }))

        execUpdateDbBtn.addMouseListener(handMouseClickListener({
            execUpdateDbBtn.icon = ScreenUtils().getIconScaled("thunder_icon_512_disable.png")
        }))

        // To avoid cursor bug
        terminalPanel.addMouseListener(defaultCursorListener())
        menuBar.addMouseListener(defaultCursorListener())
        fileMenu.addMouseListener(defaultCursorListener())

        hDocument.addUndoableEditListener {
            undoManager.addEdit(it.edit)
        }

        inputMap.put(KeyStroke.getKeyStroke(
            KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMask), "Undo")
        inputMap.put(KeyStroke.getKeyStroke(
            KeyEvent.VK_Z, Toolkit.getDefaultToolkit().menuShortcutKeyMask or InputEvent.SHIFT_MASK), "Redo")

        actionMap.put("Undo", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                try {
                    for(i in 1..2) {
                        if (undoManager.canUndo())
                            undoManager.undo()
                    }
                } catch (exp: CannotUndoException) {
                    exp.printStackTrace()
                }
            }
        })
        actionMap.put("Redo", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                try {
                    for(i in 1..2) {
                        if (undoManager.canRedo())
                            undoManager.redo()
                    }
                } catch (exp: CannotUndoException) {
                    exp.printStackTrace()
                }
            }

        })

        sqlTextPane.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                if (e.keyChar.code == KeyEvent.VK_ENTER) {
                    if (suggestion?.insertSelection(true) == true) {
                        e.consume()
                        sqlTextPane.caretColor = Color.BLACK
                    }
                }
            }

            override fun keyReleased(e: KeyEvent) {
                when {
                    e.keyCode == KeyEvent.VK_DOWN -> suggestion?.moveDown()
                    e.keyCode == KeyEvent.VK_UP -> suggestion?.moveUp()
                    e.keyCode == KeyEvent.VK_LEFT -> hideSuggestion()
                    e.keyCode == KeyEvent.VK_RIGHT -> hideSuggestion()
                    e.keyCode == KeyEvent.VK_ESCAPE -> hideSuggestion()
                    e.keyCode == KeyEvent.VK_BACK_SPACE -> showSuggestionLater()
                    '_' == e.keyChar -> showSuggestionLater()
                    Character.isLetterOrDigit(e.keyChar) -> showSuggestionLater()
                    Character.isWhitespace(e.keyChar) -> hideSuggestion()
                }
            }

            override fun keyPressed(e: KeyEvent) {
            }

        })

        sqlTextPane.addCaretListener { e ->
            if (devScreenMode) {
                println("Row: ${TextUtils.getRow(e.dot, sqlTextPane)}")
                println("Col: ${TextUtils.getColumn(e.dot, sqlTextPane)}")
            }
        }

    }

    private fun execute(command: String) {

        tablePanel.removeAll()

        SwingUtilities.invokeLater {

            val resultObject = ConnectDB.executeQuery(dbPath, command)

            if (!resultObject.hasError && resultObject.columns.isNotEmpty() &&
                resultObject.resultType == ResultTypeEnum.SELECT) {

                this.fillGrid(command, resultObject)

            } else if (!resultObject.hasError) {
                addTerminalMsg("Comando executado.")
            }

        }

    }

    private fun fillGrid(command: String, resultObject: ResultObject?) {

        resultObject?: return

        val columns: Array<String> = resultObject.columns
        val data: Array<Array<Any>> = resultObject.result
        // **** TABLE RESULT ****
        val table = JTable(data, columns)
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        val rowTable = RowNumberTable(table)
        table.model.addTableModelListener(getTableModelListener())
        tableResultScrollPane = JScrollPane(
            table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        tableResultScrollPane?.setRowHeaderView(rowTable)
        tableResultScrollPane?.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.tableHeader)
        tableResultScrollPane?.setBounds(10, 16, tablePanel.width-20, tablePanel.height-26)
        tablePanel.add(tableResultScrollPane)

        if (!resultObject.hasError) {

            addTerminalMsg("Linhas retornadas ${resultObject.result.size}")
            val args: MutableList<Any> = mutableListOf()
            args.add(getRandomHash())
            args.add(Calendar.getInstance().timeInMillis)
            args.add(command)
            ConnectDB.insertHistoric(args)

        } else {
            addTerminalMsg("Ocorreu um erro na execução ${resultObject.errorMsg}")
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
                val mouseCoords = e.locationOnScreen

                cursor = if (mouseCoords.x <= frame.x +12 && mouseCoords.y >= (mainScreenHeight.toInt()+frame.y)-20) {
                    Cursor(Cursor.NE_RESIZE_CURSOR)
                } else if (mouseCoords!!.x <= frame.x +12) {
                    mousePosition = SideMousePosition.LEFT
                    Cursor(Cursor.W_RESIZE_CURSOR)
                } else if (mouseCoords.x >= (mainScreenWidth.toInt()+frame.x)-12 &&
                    (mouseCoords.y >= (mainScreenHeight.toInt()+frame.y)-20)) {
                    Cursor(Cursor.NW_RESIZE_CURSOR)
                } else if (mouseCoords.x >= (mainScreenWidth.toInt()+frame.x)-12) {
                    mousePosition = SideMousePosition.RIGHT
                    Cursor(Cursor.E_RESIZE_CURSOR)
                } else if (mouseCoords.y >= (mainScreenHeight.toInt()+frame.y)-20) {
                    mousePosition = SideMousePosition.BOTTOM
                    Cursor(Cursor.N_RESIZE_CURSOR)
                } else {
                    mousePosition = SideMousePosition.NONE
                    Cursor(Cursor.DEFAULT_CURSOR)
                }
            }

            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)
                val mouseCoords = e?.locationOnScreen
                resize(mouseCoords)
            }

        }
    }

    private fun resize(mouseCoords: Point?) {

        if (mouseCoords == null)
            return

        when(mousePosition) {
            SideMousePosition.LEFT -> {
                val diff = this.x - mouseCoords.x
                val newScreenWidth = this.width + diff
                resizeX(newScreenWidth, mouseCoords.x)
            }
            SideMousePosition.RIGHT -> {
                val newScreenWidth = mouseCoords.x - this.x
                resizeX(newScreenWidth)
            }
            SideMousePosition.BOTTOM -> {
                val newScreenHeight = mouseCoords.y - this.y
                resizeY(newScreenHeight)
            }
            else -> {}
        }

    }

    private fun resizeX(newScreenWidth: Int, newX: Int = this.x) {
        if (newScreenWidth >= (minimumScreenWidth ?: 0.0)) {
            mainScreenWidth = newScreenWidth.toDouble()
            setBounds(newX, this.y, mainScreenWidth.toInt(), mainScreenHeight.toInt())
            pnlPrincipal.setSize(mainScreenWidth.toInt(), mainScreenHeight.toInt())
            titleBarPanel.setSize(mainScreenWidth.toInt(), 32)
            closeBtn.setLocation(mainScreenWidth.toInt() - 32, 0)
            miniBtn.setLocation(closeBtn.x - 26, 0)
            menuBar.setSize(mainScreenWidth.toInt() - 2, 24)
            pnlBtn.setSize(mainScreenWidth.toInt() - 2, 70)
            sqlPanel.setSize(mainScreenWidth.toInt() - 20, 300)
            sqlTextAreaScrollPane.setSize(sqlPanel.width - 20, sqlPanel.height - 26)
            tablePanel.setSize(mainScreenWidth.toInt() - 20, terminalPanel.y - (sqlPanel.y + sqlPanel.height) - 20)
            tableResultScrollPane?.setSize(tablePanel.width - 20, tablePanel.height - 26)
            terminalPanel.setSize(mainScreenWidth.toInt() - 20, (mainScreenHeight*.18).toInt())
            terminalTextAreaScrollPane.setSize(terminalPanel.width - 20, terminalPanel.height - 26)
        }
    }

    private fun resizeY(newScreenHeight: Int) {
        if (newScreenHeight >= (minimumScreenHeight?: 0.0)) {
            mainScreenHeight = newScreenHeight.toDouble()
            setBounds(this.x, this.y, mainScreenWidth.toInt(), newScreenHeight)
            pnlPrincipal.setSize(mainScreenWidth.toInt(), mainScreenHeight.toInt())
            sqlPanel.setSize(mainScreenWidth.toInt() - 20, (mainScreenHeight*.3).toInt())
            sqlTextAreaScrollPane.setSize(sqlPanel.width - 20, sqlPanel.height - 26)
            terminalPanel.setBounds(10, (mainScreenHeight.toInt()-180), mainScreenWidth.toInt()-20, (mainScreenHeight*.18).toInt())
            terminalTextAreaScrollPane.setSize(terminalPanel.width - 20, terminalPanel.height - 26)
            val yTmp3 = sqlPanel.y + sqlPanel.height
            tablePanel.setBounds(10, yTmp3+10, mainScreenWidth.toInt()-20, terminalPanel.y - (sqlPanel.y + sqlPanel.height)-20)
        }
    }

    private fun getMouseMotionListenerTitleBar(): MouseMotionAdapter {
        return object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {

                if (blockByModalScreen)
                    return

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
        val playBtnImg = ScreenUtils().getIconScaled(iconName).image
        val btn = JLabel(ImageIcon(getScaledImage(playBtnImg, 32, 32)))
        btn.horizontalAlignment = SwingConstants.CENTER
        btn.verticalAlignment = SwingConstants.CENTER
        btn.border = ScreenUtils().tlrbBorder(Color.GRAY)
        return  btn
    }

    private fun getTableModelListener(): TableModelListener {

        return TableModelListener {
            execUpdateDbBtn.icon = ScreenUtils().getIconScaled("thunder_icon_512.png")
        }
    }

    private fun closeApplication() {
        writeLastContent(sqlTextPane.text)
        exitProcess(0)
    }

    private fun showSuggestion() {
        hideSuggestion()
        val position: Int = sqlTextPane.caretPosition
        val location: Point = try {
            sqlTextPane.modelToView(position).location
        } catch (e2: BadLocationException) {
            e2.printStackTrace()
            return
        }
        val text: String = sqlTextPane.text
        var start = 0.coerceAtLeast(position - 1)
        while (start > 0) {
            if (!Character.isWhitespace(text[start])) {
                start--
            } else {
                start++
                break
            }
        }
        if (start > position) {
            return
        }
        var subWord = text.substring(start, position)

        subWord = subWord.replace("\n", "")

        if (subWord.length < 2) {
            return
        }

        val suggestionsFiltered = keyWords.filter { it.startsWith(subWord) }
        val suggestionsToShow = suggestionsFiltered.stream()
            .limit(20)
            .collect(Collectors.toList())

        if (suggestionsToShow.isEmpty())
            return

        if (suggestionsFiltered.size > suggestionsToShow.size)
            suggestionsToShow.add("...[+${suggestionsFiltered.size-suggestionsToShow.size}]")

        val list: JList<String> = JList(suggestionsToShow.toTypedArray())
        list.border = BorderFactory.createLineBorder(Color.GRAY, 1)
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.selectedIndex = 0

        suggestion = SuggestionPanel(sqlTextPane, list, position, subWord, location)
        SwingUtilities.invokeLater { sqlTextPane.requestFocusInWindow() }

        sqlTextPane.caretColor = Color.WHITE
    }

    private fun hideSuggestion() {
        sqlTextPane.caretColor = Color.BLACK
        if (suggestion != null) {
            suggestion!!.hide()
            suggestion = null
        }
    }

    private fun showSuggestionLater() {
        SwingUtilities.invokeLater(::showSuggestion)
    }

    private fun openBD() {

        if (sqliteRealTimeConnected)
            return

        addTerminalMsg("Carregando...")

        fileChooser.addChoosableFileFilter(filterDQLiteDB)
        val returnVal = fileChooser.showOpenDialog(this)
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            addTerminalMsg("Por favor, aguarde...")

            SwingUtilities.invokeLater {
                val file = fileChooser.selectedFile
                dbPath = file.absolutePath
                // KeyWords
                var tablesNameUpper: List<String>? = null
                var tablesNameLow: List<String>? = null
                var columnsUpperCase: List<String>? = null
                var columnsLowCase: List<String>? = null
                if (applicationConfig.tablesKeyWordEnabled) {
                    val tablesName = ConnectDB.getTablesName(dbPath)
                    tablesNameUpper = tablesName.map { it.uppercase(Locale.getDefault()) }
                    tablesNameLow = tablesName.map { it.lowercase(Locale.getDefault()) }

                    if (applicationConfig.columnsKeyWordEnabled) {
                        val columns = mutableListOf<String>()
                        for(tableName in tablesName) {
                            val names = ConnectDB.getPragma(dbPath, tableName)
                            names.forEach {
                                if (!columns.contains(it))
                                    columns.add(it)
                            }
                        }
                        columnsUpperCase = columns.map { it.uppercase(Locale.getDefault()) }
                        columnsLowCase = columns.map { it.lowercase(Locale.getDefault()) }
                    }
                }
                var keyWordsUpper: List<String>? = null
                var keyWordsLow: List<String>? = null
                if (applicationConfig.sqlKeyWordEnabled) {
                    val keyWordsList = getKeywordsArray()
                    keyWordsUpper = keyWordsList.map { it.uppercase(Locale.getDefault()) }
                    keyWordsLow = keyWordsList.map { it.lowercase(Locale.getDefault()) }
                }
                // Add keywords
                keyWords.clear()
                keyWords.addAll(concatenate(
                    columnsUpperCase?: listOf(),
                    columnsLowCase?: listOf(),
                    tablesNameUpper?: listOf(),
                    tablesNameLow?: listOf(),
                    keyWordsUpper?: listOf(),
                    keyWordsLow?: listOf()
                ))
                // Info and enables
                addTerminalMsg("Arquivo selecionado: ${file.absoluteFile}")
                title.text = "$appName - ${file.absoluteFile}"
                executeBtn.icon = ScreenUtils().getIconScaled("play_icon_512.png")
                sqlTextPane.isEditable = true
                this.thereIsSelectedDB = true
            }
        } else addTerminalMsg("Nada selecionado!")

    }

    private fun createAndOpenExportTablesBoxSelection() {
        EventQueue.invokeLater {
            ExportTableBoxSelectionScreen(this).apply {
                isVisible = true
                isAlwaysOnTop = true
            }
        }
    }

    private fun createAndOpenAboutBox() {
        EventQueue.invokeLater {
            AboutScreen(this).apply {
                isVisible = true
                isAlwaysOnTop = true
            }
        }
    }

    override fun blockModalScreen() {
        blockByModalScreen = true
    }

    override fun releaseModalScreen() {
        blockByModalScreen = false
    }

    override fun addComponentListener(l: ComponentListener?) {
        super.addComponentListener(l)
        println("componentResized")
    }

}