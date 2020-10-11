package video;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UserSessionThread extends Thread {
    private String email;
    private String ip;
    private String type;
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
                System.out.println("---------"+msg);
                MessagePacket messagePacket = new Gson().fromJson(msg, MessagePacket.class);
                String type = messagePacket.getType();
                switch (messagePacket.getEvent()) {
                    case Constants.CONNECT:
                        this.email = messagePacket.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        this.type = messagePacket.getType();
                        System.out.println("type: " + type + " ," + "Request Event: Constants.CONNECT " + email + " : " + ip);
                        server.createSession(email, ip);
                        break;
                    case Constants.GET_PATIENTS:
                        System.out.println("type: " + type + " ," + "Request Event: Constants.GET_PATIENTS " + email + " : " + ip);
                        DataOutputStream patientList = new DataOutputStream(socket.getOutputStream());
                        patientList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_PATIENTS, "receptionist").setEmailPojos(server.getPatientList())));
                        break;
                    case Constants.GET_RECEPTIONISTS:
                        System.out.println("type: " + type + " ," + "Request Event: Constants.GET_RECEPTIONISTS " + email + " : " + ip);
                        DataOutputStream receptionistList = new DataOutputStream(socket.getOutputStream());
                        receptionistList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_RECEPTIONISTS, "receptionist").setEmailPojos(server.getReceptionistList())));
                        break;
                    case Constants.GET_CONNECTIONS:
                        System.out.println("type: " + type + " ," + "Request Event: Constants.CONNECTIONS " + email + " : " + ip);
                        DataOutputStream connectionList = new DataOutputStream(socket.getOutputStream());
                        connectionList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_CONNECTIONS, "receptionist").setConnection(server.getConnectionList())));
                        break;
                    case Constants.CONNECT_TO:
                        System.out.println("type: " + type + " ," + "Request Event: Constants.CONNECT_TO " + email + " : " + ip);
                        this.ip = socket.getInetAddress().getHostName();
                        Socket patientSocket = server.connectTo(ip, messagePacket.getConnectTo());
                        MessagePacket connect = new MessagePacket("sahid@gmail.com", "receptionistavailable", "patient");
                        DataOutputStream receptionAvailable = new DataOutputStream(patientSocket.getOutputStream());
                        receptionAvailable.writeUTF(new Gson().toJson(connect));
                        break;
                    case Constants.CONNECT_TO_RECEPTIONIST:
                        System.out.println("type: " + type + " ," + "Request Event: Constants.CONNECT_TO_RECEPTIONIST " + email + " : " + ip);
                        this.email = messagePacket.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        this.type = messagePacket.getType();
                        server.createSession(email, ip);
                        if (server.hasConnection(ip)) {
                            MessagePacket available =
                                new MessagePacket(messagePacket.email, Constants.RECEPTIONIST_AVAILABLE, type);
                            DataOutputStream receptionistAvailable = new DataOutputStream(socket.getOutputStream());
                            receptionistAvailable.writeUTF(new Gson().toJson(available));
                        } else {
                            MessagePacket notAvailable =
                                new MessagePacket(messagePacket.email, Constants.RECEPTIONIST_NOT_AVAILABLE, type);
                            DataOutputStream receptionistNotAvailable = new DataOutputStream(socket.getOutputStream());
                            receptionistNotAvailable.writeUTF(new Gson().toJson(notAvailable));
                        }
                        break;
                    default:
                }
                socket.getInputStream().reset();
            } catch (IOException ex) {
                //System.out.println("Error in chat.UserThread: " + ex.getMessage());
                //ex.printStackTrace();
            }
        }
    }

    public String getIp() {
        return ip;
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public Socket getSocket() {
        return socket;
    }
}
