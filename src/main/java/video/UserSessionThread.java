package video;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
                MessagePacket messagePacket = new Gson().fromJson(msg, MessagePacket.class);
                switch (messagePacket.getEvent()) {
                    case "connecttoreceptionist":
                        this.email = messagePacket.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        if (server.createSession(email, ip)) {
                            MessagePacket connect = new MessagePacket("sahid@gmail.com", "receptionistavailable");
                            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                            dout.writeUTF(new Gson().toJson(connect));
                        } else {
                            MessagePacket connect = new MessagePacket("sahid@gmail.com", "noreceptionist");
                            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                            dout.writeUTF(new Gson().toJson(connect));
                        }
                        break;
                    case "disconnect":
                        server.removeSession();
                        break;
                    case "connactto":
                        this.ip = socket.getInetAddress().getHostName();
                        Socket patientSocket = server.connectTo(ip, messagePacket.getConnectTo());
                        MessagePacket connect = new MessagePacket("sahid@gmail.com", "receptionistavailable");
                        DataOutputStream dout = new DataOutputStream(patientSocket.getOutputStream());
                        dout.writeUTF(new Gson().toJson(connect));
                        break;
                    case "patientlist":
                        break;
                    case "receptionistlist":
                        break;
                    case "connectionlist":
                        break;
                    default:
                }
            } catch (IOException ex) {
                //System.out.println("Error in chat.UserThread: " + ex.getMessage());
                //ex.printStackTrace();
            }
        }
    }

    public String getIp() {
        return ip;
    }
}
