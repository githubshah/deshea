public class Client {
    private String ip;
    private int micPort;
    private int speakerPort;
    private int connectedPort;

    public Client(String ip, int port, PortType portType) {
        this.ip = ip;
        this.setPort(port, portType);
    }

    public Client setPort(int port, PortType portType) {
        switch (portType) {
            case SENDER:
                this.micPort = port;
                incConnectedPort(port, portType);
                break;
            case RECEIVER:
                this.speakerPort = port;
                incConnectedPort(port, portType);
                break;
            default:
                throw new IllegalArgumentException("Illegal port type exception");
        }
        return this;
    }

    private void incConnectedPort(int port, PortType portType) {
        ++connectedPort;
        if (connectedPort == 2) {
            ++connectedPort; // never call again
            System.out.println(">>>>>>>>-> completely connected ip: " + ip + " ,mic: " + micPort + " ,speaker: " + speakerPort);
        }
        if (connectedPort < 2) {
            System.out.println("<-<<<<<<<< partially connected ip: " + ip + " ,port: " + port + " ,portType: " + portType.toString());
        }
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

    public boolean isConnectedWithMicAndSpeaker() {
        return connectedPort >= 2;
    }
}
