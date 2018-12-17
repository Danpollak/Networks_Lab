package dafna_code;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientThread implements Runnable {
    private Socket clientSocket = null;
    private BufferedInputStream readFromClient;
    private BufferedOutputStream writeToClient;
    private ExecutorService executor;

    public ClientThread(Socket clientSocket, BufferedInputStream readFromClient,
                        BufferedOutputStream writeToClient, ExecutorService executor) {
        this.clientSocket = clientSocket;
        this.readFromClient = readFromClient;
        this.writeToClient = writeToClient;
        this.executor = executor;
        try {
            this.clientSocket.setSoTimeout(500);
        } catch (SocketException e) {
            System.err.println("Connection error: worker thread got timeout - " + e);
        }


    }

    @Override
    public void run() {
        // this is some code from an older tirgul, it just prints what it received from the client
        byte[] socksRequest = new byte[9]; // parse socks request
        int vn =0;
        int cd = 0;
        int dstport = 80;
        String address = "";
        Socket dest;
            try {
                // TODO: Handle bad request
                // TODO: Move to another function
                readFromClient.read(socksRequest);  //read
                vn = (socksRequest[0] & 0xFF);
                cd = (socksRequest[1] & 0xFF);
                dstport =  (socksRequest[3] | (socksRequest[2] << 8)) & 0xFFFF;
                address = "";
                address+= (int)(socksRequest[4] & 0xFF);
                for(int i=5;i<8;i++) {
                    address+= "." + (socksRequest[i] & 0xFF);
                }
                System.out.println("vn: " + vn);
                System.out.println("cd: " + cd);
                System.out.println("dstport: " + dstport);
                System.out.println("address: " + address);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            // Open new socket
        try {
            dest = new Socket(address, dstport);


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        }
    }
