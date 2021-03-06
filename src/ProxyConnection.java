/**
 * This is a runnable thread that handles both TCP connection for a single SOCKS request.
 * */

package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
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
        byte[] socksRequest = new byte[9];
        CountDownLatch latch = new CountDownLatch(1);

        // Try to receive socks request from client
        try {
            this.clientInput.read(socksRequest);
            this.socks = new Socks(socksRequest);
        } catch (IOException e) {
            System.err.println("Connection Error:" + e.getMessage());
            closeConnection();
            return;
        }

        // Handle bad SOCKS request
        if(!Objects.equals(socks.validate(), "ACK")){
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

        // Open Dest socket
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

        // Send Successful Connection prompt
        System.out.println("Successful connection from "
                + getSocketAddress(this.server) + " to " + getSocketAddress(this.destSocket));

        // Create data piping threads
        DataPipe DestToClient = new DataPipe(destInput, clientOutput, latch);
        // Send grabAuth=true if the destination port is 80
        DataPipe ClientToDest = new DataPipe(clientInput, destOutput, latch, socks.getDestinationPort() == 80);
        executor.submit(ClientToDest);
        executor.submit(DestToClient);
        try {
            latch.await();
        } catch (InterruptedException e) {
        }

        // Close all connections
        closeConnection();
    }

    // Generate byte array as socks ACK.
    private byte[] socksACK(String valid) {
        // first byte is version, 2nd byte is code, other bytes ignored.
        byte[] ACK = {0, (byte)(valid.equals("ACK") ? 90 : 91), 0, 0, 0, 0, 0, 0 };
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
