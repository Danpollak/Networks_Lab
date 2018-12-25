/**
 * This class is used to hold the SOCKS protocol data, parse it and validate it
 * */

package src;

@SuppressWarnings("StringConcatenationInLoop")
public class Socks {
    private int vn;
    private int cd;
    private int dstport;
    private String address;

    // The constructor gets a byte array and parse it
    public Socks (byte[] data){
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

    // This is a validation method to see if the data from the byte array is a valid SOCKS request
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
