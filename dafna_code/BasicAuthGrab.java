package dafna_code;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;

public class BasicAuthGrab {
    private String path;
    private String host;
    private String basicAuth;
    final String PATH_PREFIX = "GET ";
    final String PATH_SUFFIX = " HTTP/1.1\r";
    final String HOST_PREFIX = "Host: ";
    final String BASIC_AUTH_PREFIX = "Authorization: Basic ";


    public BasicAuthGrab(){
        this.path = null;
        this.host = null;
        this.basicAuth = null;
    }

    public boolean checkBasicAuth() {
        return this.path != null && this.host != null && this.basicAuth !=null;
    }

    public void parseLine(String line){
        // check for path
        if(line.startsWith(PATH_PREFIX) && line.endsWith(PATH_SUFFIX)){
            setPath(line);
        }
        // check for host
        if(line.startsWith(HOST_PREFIX)){
            setHost(line);
        }
        // check for basic auth
        if(line.startsWith(BASIC_AUTH_PREFIX)){
            setBasicAuth(line);
        }
    }

    public void setPath(String line) {
        this.path = line.substring(PATH_PREFIX.length(), line.lastIndexOf(" "));
    }

    public void setHost(String line){
        this.host = line.substring(HOST_PREFIX.length()).replace("\r","");

    }

    public void setBasicAuth(String line){
        String encodedAuth = line.substring(BASIC_AUTH_PREFIX.length()-1);
        byte[] decoded64 = DatatypeConverter.parseBase64Binary(encodedAuth);
        this.basicAuth = new String(decoded64);
    }

    @Override
    public String toString(){
    return ("Password Found! http://" + this.basicAuth + "@" + this.host + this.path);
    }
}
