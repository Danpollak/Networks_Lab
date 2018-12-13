package dafna_code;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class EchoRunnable implements Runnable {
    private Socket clientSocket = null;

    public EchoRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
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

        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            clientSentence = inFromClient.readLine();  //read
            System.out.println("Received: " + clientSentence);
//            if (clientSentence != null) {
//                capitalizedSentence = clientSentence.toUpperCase() + '\n';
//                outToClient.writeBytes(capitalizedSentence);  // write to client
//            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
            }
        }
    }
}

class TCPServerMultithreaded {
    public static void main(String argv[]) throws Exception {
        // listen to port 8080 for new clients
        ServerSocket proxySocket = new ServerSocket(8080);
        // create a thread pool with 20 threads
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            while (true) {
                // listen to client connections
                Socket clientSocket = proxySocket.accept();
                Runnable worker = new EchoRunnable(clientSocket);
                System.out.println("running new thread worker");
                executor.execute(worker);
            }
        } catch (Exception e) {
            System.err.println("Connection error: couldn't assign threads to clients - got error " + e);
            executor.shutdown();
        }

    }
}