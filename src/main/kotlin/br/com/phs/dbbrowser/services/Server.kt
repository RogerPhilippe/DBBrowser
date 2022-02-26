package br.com.phs.dbbrowser.services

import br.com.phs.dbbrowser.data.models.SQLiteRTResult
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.SwingWorker

class Server {

    // Private attributes
    private var server: ServerSocket? = null
    private var client: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var line: String = ""

    // Public attributes
    var connected = false
    var connecting = false

    fun initServer(port: Int = 4500, done: (status: Boolean)->Unit) {

        this.connecting = true

        try {
            server = ServerSocket(port)
            println("Listen on Port 4500")
        } catch (ex: Exception) {
            println("Could not listen on port 4500")
            ex.printStackTrace()
            done(false)
            return
        }

        try {
            client = server?.accept()
            println("Client Accepted")
        } catch (ex: Exception) {
            println("Accept failed or canceled: 4500")
            ex.printStackTrace()
            done(false)
            return
        }

        connecting = false
        connected = true
        done(true)

    }

    fun close() {

        connecting = false
        connected = false

        client?.close()
        server?.close()

    }

    fun sendCommand(command: String, done: (result: SQLiteRTResult?)->Unit) {

        try {

            client?.let {
                writer = PrintWriter(it.getOutputStream(), true)
            }

        } catch (ex: Exception) {
            println("Writer failed")
            ex.printStackTrace()
            return
        }

        writer?.println(command)

        object : SwingWorker<Boolean, Void>() {
            override fun doInBackground(): Boolean {
                val result = listenSocket()
                done(result)
                return true
            }

        }.execute()

    }

    private fun listenSocket(): SQLiteRTResult? {

        println("Send, waiting for a response...")
        var result: SQLiteRTResult? = null

        while (true) {
            try {

                try {
                    client?.let {
                        reader = BufferedReader(InputStreamReader(it.getInputStream()))
                    }
                } catch (ex: Exception) {
                    println("Read failed")
                    ex.printStackTrace()
                    break
                }

                line = reader?.readLine()?: continue

                val dateTimeNow = Calendar.getInstance().time
                val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy")

                println("[${dateTimeFormat.format(dateTimeNow)}]: $line")
                val jsonObject = JSONObject(line)
                val statusCode = jsonObject["status"].toString()
                result = SQLiteRTResult(statusCode == "200", jsonObject)

                break

            } catch (ex: Exception) {
                println("Read failed: ${ex.message}")
                ex.printStackTrace()
                result = SQLiteRTResult(false, null)
                break
            }
        }

        return result

    }

}