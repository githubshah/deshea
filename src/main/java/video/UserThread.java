package video;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class UserThread extends Thread {
    public Socket getSocket() {
        return socket;
    }

    private Socket socket;
    private ChatServer server;

    public UserThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        while (true) {
            try {
                DataInputStream din = new DataInputStream(socket.getInputStream());

                int len = din.readInt();
                byte[] data = new byte[len];
                if (len > 0) {
                    din.readFully(data);
                }
                System.out.println("data received: " + len);
                server.broadcast(data, socket);
                System.out.println("data send: " + len);
            } catch (IOException ex) {
                //System.out.println("Error in chat.UserThread: " + ex.getMessage());
                //ex.printStackTrace();
            }
        }
    }
}