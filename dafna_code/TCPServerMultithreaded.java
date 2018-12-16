package dafna_code;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientThread implements Runnable {
    private Socket clientSocket = null;
    private BufferedReader readFromClient;
    private DataOutputStream writeToClient;

    public ClientThread(Socket clientSocket, BufferedReader readFromClient, DataOutputStream writeToClient) {
        this.clientSocket = clientSocket;
        this.readFromClient = readFromClient;
        this.writeToClient = writeToClient;
        try {
            this.clientSocket.setSoTimeout(500);
        } catch (SocketException e) {
            System.err.println("Connection error: worker thread got timeout - " + e);
        }


    }

    @Override
    public void run() {
        // this is some code from an older tirgul, it just prints what it received from the client
        String clientSentence;

         try {
            clientSentence = readFromClient.readLine();  //read
            System.out.println("Received: " + clientSentence);
            if (clientSentence != null) {
                writeToClient.writeBytes(clientSentence.toUpperCase() + '\n');  // write to client
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

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

                // listen to client connections
                Socket clientSocket = proxySocket.accept();
                // open data streams to read and write to client
                BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());

                // init threads
                Runnable clientThread = new ClientThread(clientSocket, inFromClient, outToClient);

                System.out.println("running new thread worker");
                // run threads
                executor.execute(clientThread);
            }
        } catch (Exception e) {
            System.err.println("Connection error: couldn't assign threads to clients - got error " + e);
            executor.shutdown();
        }

    }
}