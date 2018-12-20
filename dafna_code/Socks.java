package dafna_code;

public class Socks {
    private int vn;
    private int cd;
    private int dstport;
    private String address;
    private boolean valid;

    public Socks (byte[] data){
        // TODO: Handle bad requests. if bad, valid is false;
        this.vn = (data[0] & 0xFF);
        this.cd = (data[1] & 0xFF);
        this.dstport =  (data[3] | (data[2] << 8)) & 0xFFFF;
        this.address = "";
        this.address+= (int)(data[4] & 0xFF);
        for(int i=5;i<8;i++) {
            this.address+= "." + (data[i] & 0xFF);
        }
        this.valid = true;
    }

    public int getVersion(){
        return this.vn;
    }

    public int getCode(){
        return this.cd;
    }

    public int getDestinationPort(){
        return this.dstport;
    }

    public String getAddress(){
        return this.address;
    }

    public boolean isValid() {
        return this.valid;
    }

    public String toString() {
        return ("vn: " + vn + "\ncd: " + cd + "\ndstport: " + dstport + "\naddress:" + address);
    }
}
