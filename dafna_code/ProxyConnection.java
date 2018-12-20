package dafna_code;

import com.sun.security.ntlm.Client;
import sun.rmi.transport.tcp.TCPConnection;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProxyConnection implements Runnable {
    private Socket clientSocket;
    private Socket destSocket;
    private InputStream clientInput;
    private InputStream destInput;
    private OutputStream clientOutput;
    private OutputStream destOutput;
    private Socks socks;
    ExecutorService executor = Executors.newFixedThreadPool(2);


    public ProxyConnection(Socket clientSocket, InputStream clientInput, OutputStream clientOutput){
        this.clientSocket = clientSocket;
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
    }

    public void run(){
        // get socks request from client
        byte[] socksRequest = new byte[9];
        CountDownLatch latch = new CountDownLatch(1);

        try {
            // TODO: Handle bad request
            this.clientInput.read(socksRequest);  //read
            System.out.println("Read Socks Request");
            System.out.println(socksRequest[0]);
            this.socks = new Socks(socksRequest);
            System.out.println("Initialized socks class");
            System.out.println(this.socks.toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        // Send SOCKS ACK to client
        try {
            this.clientOutput.write(this.socksACK(this.socks.isValid()));
            this.clientOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create Dest socket
        try {
            this.destSocket= new Socket(socks.getAddress(), socks.getDestinationPort());
            this.destInput = this.destSocket.getInputStream();
            this.destOutput = this.destSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create data piping
        DataPipe ClientToDest = new DataPipe(clientInput, destOutput, "clientToDest", latch);
        DataPipe DestToClient = new DataPipe(destInput, clientOutput, "destToClient", latch);
        executor.submit(ClientToDest);
        executor.submit(DestToClient);
        try {
            latch.await();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close all connections
        try {
            this.destSocket.shutdownInput();
            this.destSocket.shutdownOutput();
            this.clientSocket.shutdownInput();
            this.clientSocket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                this.clientSocket.close();
                this.destSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ended proxy");
    }

    private byte[] socksACK(boolean valid) {
        // first byte is version, 2nd byte is code, other bytes ignored.
        byte[] ACK = {0, (byte)(valid ? 90 : 91), 0, 0, 0, 0, 0, 0 };
        return ACK;
    }
}
