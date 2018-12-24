package dafna_code;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyConnection implements Runnable {
    private ServerSocket server;
    private Socket clientSocket;
    private Socket destSocket;
    private InputStream clientInput;
    private InputStream destInput;
    private OutputStream clientOutput;
    private OutputStream destOutput;
    private Socks socks;
    ExecutorService executor = Executors.newFixedThreadPool(2);


    public ProxyConnection(ServerSocket server, Socket clientSocket, InputStream clientInput, OutputStream clientOutput){
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
    }

    public void run(){
        // get socks request from client
        byte[] socksRequest = new byte[9];
        CountDownLatch latch = new CountDownLatch(1);

        try {
            this.clientInput.read(socksRequest);  //read
            this.socks = new Socks(socksRequest);
        } catch (IOException e) {
            System.err.println("Connection Error:" + e.getMessage());
            closeConnection();
            return;
        }

        // Handle bad socks values
        if(socks.validate() != "ACK"){
            System.err.println("Connection Error: While parsing SOCKS request: " + socks.validate());
            closeConnection();
            return;
        }
        // Send SOCKS ACK to client
        try {
            this.clientOutput.write(this.socksACK(this.socks.validate()));
            this.clientOutput.flush();
        } catch (IOException e) {
            System.err.println("Connection Error: Failed to send SOCKS ACK to client");
            closeConnection();
            return;
        }

        // Create Dest socket
        try {
            this.destSocket= new Socket(socks.getAddress(), socks.getDestinationPort());
            this.destInput = this.destSocket.getInputStream();
            this.destOutput = this.destSocket.getOutputStream();
            this.destSocket.setSoTimeout(500);
        } catch (IOException e) {
            System.err.println("Connection Error: Failed to connect to host");
            closeConnection();
            return;
        }
        // Send Successful Connection
        System.out.println("Successful connection from "
                + getSocketAddress(this.server) + " to " + getSocketAddress(this.destSocket));

        // Create data piping
        DataPipe ClientToDest = new DataPipe(clientInput, destOutput, latch, true);
        DataPipe DestToClient = new DataPipe(destInput, clientOutput, latch);
        executor.submit(ClientToDest);
        executor.submit(DestToClient);
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        // Close all connections
        closeConnection();
    }

    private byte[] socksACK(String valid) {
        // first byte is version, 2nd byte is code, other bytes ignored.
        byte[] ACK = {0, (byte)(valid == "ACK" ? 90 : 91), 0, 0, 0, 0, 0, 0 };
        return ACK;
    }

    private String getSocketAddress(Socket s){
        return s.getLocalSocketAddress().toString().substring(1);
    }

    private String getSocketAddress(ServerSocket s){
        String str = s.getLocalSocketAddress().toString();
        return str.substring(str.indexOf('/')+1);

    }

    private void closeConnection(){
        // close threads
        executor.shutdown();

        // try to close host socket (if open)
        if(this.destSocket != null && !this.destSocket.isClosed()){
            try{
                this.destSocket.shutdownInput();
                this.destSocket.shutdownOutput();
                this.destSocket.close();
            } catch (IOException e) {
            }
        }

        // try to close client socket
        if(!this.clientSocket.isClosed()){
            try{
                this.clientSocket.shutdownInput();
                this.clientSocket.shutdownOutput();
                this.clientSocket.close();
            } catch (IOException e) {
            }
        }

        System.out.println("Closing connection from " + getSocketAddress(this.server));
    }
}
