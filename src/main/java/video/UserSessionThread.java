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
                MessagePacket messagePacket = new Gson().fromJson(msg, MessagePacket.class);
                switch (messagePacket.getEvent()) {
                    case Constants.CONNECT:
                        this.email = messagePacket.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        this.type = messagePacket.getType();
                        System.out.println("Request Event: Constants.CONNECT " + email + " : " + ip);
                        server.createSession(email, ip);
                        break;
                    case Constants.GET_PATIENTS:
                        System.out.println("Request Event: Constants.GET_PATIENTS " + email + " : " + ip);
                        DataOutputStream patientList = new DataOutputStream(socket.getOutputStream());
                        patientList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_PATIENTS, "receptionist").setEmailPojos(server.getPatientList())));
                        break;
                    case Constants.GET_RECEPTIONISTS:
                        System.out.println("Request Event: Constants.GET_RECEPTIONISTS " + email + " : " + ip);
                        DataOutputStream receptionistList = new DataOutputStream(socket.getOutputStream());
                        receptionistList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_RECEPTIONISTS, "receptionist").setEmailPojos(server.getReceptionistList())));
                        break;
                    case Constants.GET_CONNECTIONS:
                        System.out.println("Request Event: Constants.CONNECTIONS " + email + " : " + ip);
                        DataOutputStream connectionList = new DataOutputStream(socket.getOutputStream());
                        connectionList.writeUTF(new Gson().toJson(
                            new MessagePacket(email, Constants.GET_CONNECTIONS, "receptionist").setConnection(server.getConnectionList())));
                        break;
                    case Constants.CONNECT_TO:
                        System.out.println("Request Event: Constants.CONNECT_TO " + email + " : " + ip);
                        this.ip = socket.getInetAddress().getHostName();
                        Socket patientSocket = server.connectTo(ip, messagePacket.getConnectTo());
                        MessagePacket connect = new MessagePacket("sahid@gmail.com", "receptionistavailable", "patient");
                        DataOutputStream receptionAvailable = new DataOutputStream(patientSocket.getOutputStream());
                        receptionAvailable.writeUTF(new Gson().toJson(connect));
                        break;
                    case Constants.CONNECT_TO_RECEPTIONIST:
                        System.out.println("Request Event: Constants.CONNECT_TO_RECEPTIONIST " + email + " : " + ip);
                        this.email = messagePacket.getEmail();
                        this.ip = socket.getInetAddress().getHostName();
                        server.createSession(email, ip);
                        if (server.hasConnection(ip)) {
                            MessagePacket connect124 =
                                new MessagePacket("sahid@gmail.com", Constants.RECEPTIONIST_AVAILABLE, "patient");
                            DataOutputStream receptionist_Available = new DataOutputStream(socket.getOutputStream());
                            receptionist_Available.writeUTF(new Gson().toJson(connect124));
                        } else {
                            MessagePacket connect13 =
                                new MessagePacket("sahid@gmail.com", Constants.RECEPTIONIST_NOT_AVAILABLE, "patient");
                            DataOutputStream dout13 = new DataOutputStream(socket.getOutputStream());
                            dout13.writeUTF(new Gson().toJson(connect13));
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
