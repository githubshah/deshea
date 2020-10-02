public class Client {
    private String ip;
    private int micPort;
    private int speakerPort;

    public Client(String ip, int port, PortType portType) {
        this.ip = ip;
        this.setPort(port, portType);
        System.out.println(">>> SessionCreated IP : " + ip);
    }

    public Client setPort(int port, PortType portType) {
        switch (portType) {
            case SENDER:
                this.micPort = port;
                break;
            case RECEIVER:
                this.speakerPort = port;
                break;
            default:
                throw new IllegalArgumentException("Illegal port type exception");
        }
        return this;
    }

    public String getIp() {
        return ip;
    }

    public int getMicPort() {
        return micPort;
    }

    public int getSpeakerPort() {
        return speakerPort;
    }
}
