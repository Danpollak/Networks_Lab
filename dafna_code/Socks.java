package dafna_code;

public class Socks {
    private int vn;
    private int cd;
    private int dstport;
    private String address;

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

    public String validate() {
        if(vn != 4){
            return "Unsupported SOCKS protocol version (got " + vn + ")";
        }
        if(cd != 1){
            return "Command code not recognized";
        }
        if(dstport < 0){
            return "Invalid port";
        }
        return "ACK";
    }

    public String toString() {
        return ("vn: " + vn + "\ncd: " + cd + "\ndstport: " + dstport + "\naddress:" + address);
    }
}
