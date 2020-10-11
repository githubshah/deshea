package video;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoServer {
    private Set<VideoThread> videoThreadPool = new HashSet<>(); // ip
    private Set<UserSessionThread> userSessionThreadPool = new HashSet<>(); // ip, email
    BidiMap conferenceMap = new DualHashBidiMap(); // receptionistIp => clientIp
    private Map<String, String> activeUserMap = new HashMap(); // email, ip

    public void openVideoServerSocket() {
        try (ServerSocket serverSocket = new ServerSocket(9898)) {
            System.out.println("Chat Server is listening on port " + 9898);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");
                VideoThread newUser = new VideoThread(socket, this);
                videoThreadPool.add(newUser);
                newUser.start();
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void openInfoServerSocket() {
        try (ServerSocket serverSocket = new ServerSocket(9897)) {

            System.out.println("Chat Server is listening on port " + 9897);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("requested user IP: " + socket.getInetAddress().getHostName());
                if (userSessionThreadPool
                    .stream().peek(x -> System.out.println("check already user IP: " + socket.getInetAddress().getHostName()))
                    .anyMatch(x -> socket.getInetAddress().getHostName().equals(x.getIp()))
                ) {
                    System.out.println("User already is in session");
                } else {
                    System.out.println("Chat New user connected");
                    UserSessionThread newUser = new UserSessionThread(socket, this);
                    userSessionThreadPool.add(newUser);
                    newUser.start();
                }
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        new Thread(() -> {
//            VideoServer server = new VideoServer();
//            server.openVideoServerSocket();
//        }).start();
//
//        new Thread(() -> {
//            VideoServer server1 = new VideoServer();
//            server1.openInfoServerSocket();
//
//        }).start();
//
//    }

    public void broadcast(byte[] data, Socket from) {
        System.out.println("userThreads size: " + videoThreadPool.size());
        if (videoThreadPool.size() == 1) {
            sendMsg(data, from);
        } else {
            videoThreadPool.forEach(socket -> {
                Socket to = socket.getSocket();
                if (!from.getInetAddress().getHostAddress().equals(to.getInetAddress().getHostAddress())) {
                    sendMsg(data, to);
                }
            });
        }
    }

    public void peerToPeer(byte[] data, Socket from) { // from client or receptionist
        System.out.println("peerToPeer userThreads size: " + videoThreadPool.size());
        String requestIP = from.getInetAddress().getHostAddress();
        String otherUserIp = (String) conferenceMap.get(requestIP);
        if (otherUserIp == null) {
            otherUserIp = (String) conferenceMap.getKey(requestIP);
        }

        final Socket[] socket = new Socket[1];
        String finalOtherUserIp = otherUserIp;
        if (finalOtherUserIp == null) {
            System.out.println("not have any connection");
            return;
        }
        videoThreadPool.forEach(t -> {
            // otherUserIp => connect user
            if (t.getSocket().getInetAddress().getHostAddress().equals(finalOtherUserIp)) {
                socket[0] = t.getSocket();
            }
        });
        sendMsg(data, socket[0]);
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

    public void removeSession() {

    }

    public Socket connectTo(String ip, String email) {
        System.out.println("Create connection between " + ip + " : " + email);
        String patientIp = activeUserMap.get(email);
        conferenceMap.put(ip, patientIp); // receptionistIp => clientIp

        for (UserSessionThread x : userSessionThreadPool) {
            if (x.getIp().equals(patientIp)) {
                return x.getSocket();
            }
        }
        return null;
    }

    public void createSession(String email, String ip) {
        System.out.println("User is under session: " + email);
        activeUserMap.put(email, ip);
    }

    public boolean hasConnection(String ip) {
        return (String) conferenceMap.get(ip) != null || (String) conferenceMap.getKey(ip) != null;
    }

    public List<String> getPatientList() {
        System.out.println(userSessionThreadPool.size());
        return userSessionThreadPool
            .stream()
            .peek(x -> System.out.println("getPatientList: " + x.getType()))
            .filter(useSessionThread -> ("patient").equals(useSessionThread.getType()))
            .map(UserSessionThread::getEmail)
            .collect(Collectors.toList());
    }

    public List<String> getReceptionistList() {
        List<String> collect = userSessionThreadPool
            .stream()
            .map(UserSessionThread::getEmail)
            .collect(Collectors.toList());
        System.out.println("getReceptionistList: " + collect);
        return collect;
    }

    public Map<String, String> getConnectionList() {
        Map<String, String> map = new HashMap<>();
        activeUserMap.forEach(map::put);
        return map;
    }
}