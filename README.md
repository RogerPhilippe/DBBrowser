Exemplos de argumentos. é preferível adicionar aspas quando executado em linha de comando.
/pathDB/dbname.db "select cIDProduct, cIDCompany from MC1_Product LIMIT 10"
/pathDB/dbname.db "select * from MC1_Product LIMIT 10"

SQLite Realtime:

Na classe application, adicionar a seguinte linha:
    ClientConnection().awaitCommand()

Classe ClientConnection:

    import java.io.BufferedReader
    import java.io.InputStreamReader
    import java.io.PrintWriter
    import java.net.Socket

    class ClientConnection {
    
        companion object {
            private const val TAG = "ClientConnection"
        }
    
        private var defaultIP = "10.0.2.2"
        private var defaultPort = 4500
        private var socket: Socket? = null
        private var writer: PrintWriter? = null
        private var reader: BufferedReader? = null
    
        fun awaitCommand(ip: String = defaultIP, port: Int = defaultPort) {
            doInBackground(ip, port)
        }
    
        private fun doInBackground(ip: String, port: Int) {
    
            Thread {
    
                try {
    
                    this@ClientConnection.socket = Socket(ip, port)
                    this@ClientConnection.socket?.let {
                        this@ClientConnection.writer = PrintWriter(it.getOutputStream(), true)
                        this@ClientConnection.reader = BufferedReader(InputStreamReader(it.getInputStream()))
                    }
    
                } catch (ex: Exception) {
                    Timber.tag(TAG).e(ex)
                }
    
                var commandReceived: String
    
                while (true) {
    
                    if (this@ClientConnection.socket?.isClosed == true) {
                        Timber.tag(TAG).e("Conexão fechada")
                        break
                    }
    
                    commandReceived = this@ClientConnection.reader?.readLine()?: continue
                    this@ClientConnection.writer?.println("Return query: ${executeQuery(commandReceived)}")
    
                }
    
                try {
                    this@ClientConnection.socket?.close()
                } catch (ex: Exception) {
                    Timber.tag(TAG).e(ex)
                }
    
            }.start()
    
        }
    
        private fun executeQuery(command: String): String {
    
            return ""
    
        }
    
    }