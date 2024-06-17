import java.io.*
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

fun main() {
    val serverSocket = ServerSocket(12345)
    val clientSockets = ConcurrentHashMap.newKeySet<Socket>()
    val userNames = ConcurrentHashMap<Socket, String>()
    var userCounter = 1

    println("Server started and waiting for connection...")

    while (true) {
        val clientSocket = serverSocket.accept()
        val userName = "User$userCounter"
        userCounter++

        clientSockets.add(clientSocket)
        userNames[clientSocket] = userName

        println("$userName connected")

        thread {
            handleClient(clientSocket, clientSockets, userNames)
        }
    }
}

fun handleClient(clientSocket: Socket, clientSockets: MutableSet<Socket>, userNames: ConcurrentHashMap<Socket, String>) {
    try {
        val `in` = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val out = PrintWriter(clientSocket.getOutputStream(), true)
        val userName = userNames[clientSocket]

        var message: String?
        while (`in`.readLine().also { message = it } != null) {
            println("$userName: $message")
            clientSockets.forEach { socket ->
                if (socket != clientSocket) {
                    PrintWriter(socket.getOutputStream(), true).println("$userName: $message")
                }
            }
        }
    } catch (e: IOException) {
        println("Error: ${e.message}")
    } finally {
        try {
            clientSocket.close()
        } catch (e: IOException) {
            println("Error while closing socket: ${e.message}")
        }
        clientSockets.remove(clientSocket)
        val userName = userNames.remove(clientSocket)
        println("$userName disconnected")
        clientSockets.forEach { socket ->
            PrintWriter(socket.getOutputStream(), true).println("$userName disconnected")
        }
    }
}
