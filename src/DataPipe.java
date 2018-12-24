package src;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class DataPipe implements Runnable {
    private InputStream input;
    private OutputStream output;
    private CountDownLatch latch;
    private int data;
    private StringBuilder line;
    private BasicAuthGrab grabber;
    private boolean grabAuth;

    public DataPipe(InputStream input, OutputStream output, CountDownLatch latch){
        this.input = input;
        this.output = output;
        this.data = 0;
        this.latch = latch;
    }

    public DataPipe(InputStream input, OutputStream output, CountDownLatch latch, boolean grabAuth){
        this.input = input;
        this.output = output;
        this.data = 0;
        this.latch = latch;
        this.grabber = new BasicAuthGrab();
        this.grabAuth = grabAuth;
    }

    public void run(){
        line = new StringBuilder();
        try {
            data = input.read();
            while(data != -1){
                output.write(data);
                if(this.grabAuth) {
                    if ((char) data != '\n') {
                        line.append((char) data);
                    } else {
                        handleAuthGrab(line.toString());
                        line = new StringBuilder();
                    }
                }
                data = input.read();
            }
        } catch (IOException e) {
            if(!e.getMessage().equals("Socket closed")){
                System.err.println("Connection Error:" + e.getMessage());
            }
        }
        latch.countDown();
    }

    private void handleAuthGrab(String line){
        this.grabber.parseLine(line);
        if (this.grabber.checkBasicAuth()) {
            System.out.println(this.grabber.toString());
            this.grabAuth = false;
        }
    }
}
