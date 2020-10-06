package video;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the chat server program.
 * Press Ctrl + C to terminate the program.
 *
 * @author www.codejava.net
 */
public class ChatServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(9898);
        server.execute();
    }

    public void broadcast(byte[] data, Socket from) {
        System.out.println("userThreads size: " + userThreads.size());
        if (userThreads.size() == 1) {
            sendMsg(data, from);
        } else {
            userThreads.forEach(socket -> {
                Socket to = socket.getSocket();
                if (!from.getInetAddress().getHostAddress().equals(to.getInetAddress().getHostAddress())) {
                    sendMsg(data, to);
                }
            });
        }
    }

    private void sendMsg(byte[] data, Socket from) {
        try {
            DataOutputStream dout = new DataOutputStream(from.getOutputStream());
            dout.writeInt(data.length);
            if (data.length > 0) {
                dout.write(data, 0, data.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}