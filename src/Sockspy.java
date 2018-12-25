/**
 * Main server class.
 * */

package src;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
class Sockspy {
    static final int SERVER_PORT = 8080;
    static final int MAX_CONNECTIONS = 20;
    public static void main(String argv[]) throws Exception {
        // listen to port 8080 for new clients
        ServerSocket proxySocket = new ServerSocket(SERVER_PORT);
        // create a thread pool with 20 threads
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);
        try {
            while (true) {
                // The serverSocket waits for a client to connect to server, then assigns it a thread
                Socket clientSocket = proxySocket.accept();

                // Open the data streams and set the timeout
                InputStream inFromClient = clientSocket.getInputStream();
                OutputStream outToClient = clientSocket.getOutputStream();
                clientSocket.setSoTimeout(500);

                // Create a connection thread class
                Runnable proxyConnection = new ProxyConnection(proxySocket, clientSocket, inFromClient, outToClient);

                // Add thread to executor queue
                executor.execute(proxyConnection);
            }
        } catch (Exception e) {
            System.err.println("Connection Error: Failed to connect to client");
        }

    }
}