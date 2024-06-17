import java.io.*
import java.net.*
import kotlin.concurrent.thread

class ChatClient {
    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null
    private val console = BufferedReader(InputStreamReader(System.`in`))

    @Volatile
    private var running = true

    fun start() {
        connectToServer()

        thread {
            while (running) {
                try {
                    var serverMessage: String?
                    while (`in`?.readLine().also { serverMessage = it } != null) {
                        println(serverMessage)
                    }
                } catch (e: IOException) {
                    if (running) {
                        println("Connection error: ${e.message}")
                    }
                }
            }
        }

        while (running) {
            try {
                var userMessage: String?
                while (console.readLine().also { userMessage = it } != null) {
                    if (userMessage == "/disconnect") {
                        disconnectFromServer()
                    } else if (userMessage == "/reconnect") {
                        reconnectToServer()
                    } else {
                        out?.println(userMessage)
                    }
                }
            } catch (e: IOException) {
                println("Error while sending message: ${e.message}")
            }
        }
    }

    private fun connectToServer() {
        try {
            socket = Socket("localhost", 12345)
            out = PrintWriter(socket!!.getOutputStream(), true)
            `in` = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            println("Connection successful. Your message:")
        } catch (e: IOException) {
            println("Connection error: ${e.message}")
        }
    }

    private fun disconnectFromServer() {
        try {
            running = false
            socket?.close()
            println("Disconnected from server.")
        } catch (e: IOException) {
            println("Error while disconnecting: ${e.message}")
        }
    }

    private fun reconnectToServer() {
        disconnectFromServer()
        running = true
        connectToServer()
    }
}

fun main() {
    val client = ChatClient()
    client.start()
}
