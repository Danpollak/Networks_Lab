/**
 * This is a runnable thread that handles data piping between an input and output stream.
 * */

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

    // constructor for data pipe with not need for authentication grabbing
    public DataPipe(InputStream input, OutputStream output, CountDownLatch latch){
        this.input = input;
        this.output = output;
        this.data = 0;
        this.latch = latch;
    }

    // constructor for data pipe with authentication grabbing
    public DataPipe(InputStream input, OutputStream output, CountDownLatch latch, boolean grabAuth){
        this.input = input;
        this.output = output;
        this.data = 0;
        this.latch = latch;
        this.grabber = new BasicAuthGrab();
        this.grabAuth = grabAuth;
    }

    public void run(){
        // line will accumulate the characters until end of line
        line = new StringBuilder();
        try {
            data = input.read();
            // run while the input data is not -1 (closed stream)
            while(data != -1){
                output.write(data);
                // if we are doing authentication grabbing, append the characters until end of line
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
        // use the BasicAuthGrab class to parse the line
        this.grabber.parseLine(line);
        // if the BasicAuthGrab has all the data for printing the password, print it and stop grabbing data
        if (this.grabber.checkBasicAuth()) {
            System.out.println(this.grabber.toString());
            this.grabAuth = false;
        }
    }
}
