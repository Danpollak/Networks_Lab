package src;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
class Sockspy {
    public static void main(String argv[]) throws Exception {
        // listen to port 8080 for new clients
        ServerSocket proxySocket = new ServerSocket(8080);
        // create a thread pool with 20 threads
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            while (true) {
                // there is currently no functionality for the dest host part- this program currently reads the
                // request from the client, prints it in the server, and writes it bach to the client, capitalized.
                Socket clientSocket = proxySocket.accept();
                // listen to client connections
                // open data streams to read and write to client
                InputStream inFromClient = clientSocket.getInputStream();
                OutputStream outToClient = clientSocket.getOutputStream();
                clientSocket.setSoTimeout(500);

                // init threads
                Runnable proxyConnection = new ProxyConnection(proxySocket, clientSocket, inFromClient, outToClient);

                // run threads
                executor.execute(proxyConnection);
            }
        } catch (Exception e) {
            System.err.println("Connection Error: Failed to connect to client");
        }

    }
}