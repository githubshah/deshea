package video;

import java.net.Socket;

public class ConnectedUser {
    String emailTo;
    String ip;
    Socket socket;

    public ConnectedUser(String emailTo, String ip, Socket socket) {
        this.emailTo = emailTo;
        this.ip = ip;
        this.socket = socket;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
