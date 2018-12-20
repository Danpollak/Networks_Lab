package dafna_code;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class DataPipe implements Runnable {
    private InputStream input;
    private OutputStream output;
    private String name;
    private CountDownLatch latch;
    private int data;

    public DataPipe(InputStream input, OutputStream output,
                    String name, CountDownLatch latch){
        this.input = input;
        this.output = output;
        this.name = name;
        this.data = 0;
        this.latch = latch;
    }

    public void run(){
        try {
            System.out.println("start " + this.name);
            data = input.read();
            while(data != -1){
                output.write(data);
                data = input.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("end " + this.name);
        latch.countDown();
    }
}
