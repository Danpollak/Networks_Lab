package dafna_code;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class TCPServerMultithreaded {
    public static void main(String argv[]) throws Exception {
        // listen to port 8080 for new clients
        ServerSocket proxySocket = new ServerSocket(8080);
        // create a thread pool with 40 threads
        ExecutorService executor = Executors.newFixedThreadPool(40);
        try {
            while (true) {
                // there is currently no functionality for the dest host part- this program currently reads the
                // request from the client, prints it in the server, and writes it bach to the client, capitalized.
                Socket clientSocket = proxySocket.accept();
                // listen to client connections
                // open data streams to read and write to client
                InputStream inFromClient = clientSocket.getInputStream();
                OutputStream outToClient = clientSocket.getOutputStream();

                // init threads
                Runnable proxyConnection = new ProxyConnection(clientSocket, inFromClient, outToClient);

                System.out.println("running new thread worker");
                // run threads
                executor.execute(proxyConnection);
            }
        } catch (Exception e) {
            System.err.println("Connection error: couldn't assign threads to clients - got error " + e);
            executor.shutdown();
        }

    }
}