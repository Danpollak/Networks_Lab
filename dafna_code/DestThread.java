package dafna_code;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

public class DestThread implements Runnable {
    private Socket destSocket = null;
    private BufferedInputStream readFromDest;
    private BufferedOutputStream writeToDest;

    public DestThread(Socket destSocket, BufferedInputStream readFromDest,
                        BufferedOutputStream writeToDest) {
        this.destSocket = destSocket;
        this.readFromDest = readFromDest;
        this.writeToDest = writeToDest;
        try {
            this.destSocket.setSoTimeout(500);
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

