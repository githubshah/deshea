package video;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class UserSessionThread extends Thread {
    private String email;
    private String ip;

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;
    private VideoServer server;

    public UserSessionThread(Socket socket, VideoServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        while (true) {
            try {
                DataInputStream din = new DataInputStream(socket.getInputStream());
                String msg = din.readUTF();
                Client client = new Gson().fromJson(msg, Client.class);
                switch (client.getEvent()) {
                    case "connect":
                        this.email = client.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        server.createSession(email, ip);
                        break;
                    case "disconnect":
                        server.removeSession();
                        break;
                    case "connactto":
                        this.ip = socket.getInetAddress().getHostName();
                        server.connectTo(ip, client.getConnectTo());
                        break;
                    default:
                }
            } catch (IOException ex) {
                //System.out.println("Error in chat.UserThread: " + ex.getMessage());
                //ex.printStackTrace();
            }
        }
    }
}
