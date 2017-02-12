package net.lr.tinyhttp;

public class ProtocolVersion {
    String protocol;
    String version;
    
    public ProtocolVersion(String protocol, String version) {
        this.protocol = protocol;
        this.version = version;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return protocol + "/" + version;
    }
}
