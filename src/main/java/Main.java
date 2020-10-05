
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    final int serverPort = 9785;
    DatagramSocket udpServerSocket;
    ByteArrayOutputStream byteArrayOutputStream;

    public void runVOIP() {
        try {
            udpServerSocket = new DatagramSocket(serverPort);
            System.out.println("Server started on " + serverPort);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[40000];
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            ThreadPoolExecutor executor1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(tempBuffer, tempBuffer.length);
                try{
                    udpServerSocket.setSoTimeout(5000);
                    udpServerSocket.receive(receivePacket);
                }catch (Exception e){
                    System.out.println("drop waiting...");
                    continue;
                }
                try {
                    createOrGetSession(receivePacket);
                    byte[] data = Arrays.copyOf(tempBuffer, tempBuffer.length);
                    InetAddress callerAddress = receivePacket.getAddress();
                    executor.execute(() -> {
                        sendToClient(callerAddress, data);
                    });
                } catch (Exception e) {
                    System.out.println(e);
                    //System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(InetAddress callerAddress, byte[] data) {
        if (session.size() == 1) {
            if (session.containsKey("127.0.0.1")) {
                try {
                    int speakerPort = session.get("127.0.0.1").getSpeakerPort();
                    if (speakerPort != 0) {
                        System.out.println("data sent back to localhost: " + callerAddress.getHostAddress() + ":" + speakerPort);
                        udpServerSocket.send(new DatagramPacket(data, 0, data.length, callerAddress, speakerPort));
                    } else {
                        System.out.println("Yet to be connect => No Speaker port available: " + callerAddress.getHostAddress() + ":" + speakerPort);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    int speakerPort = session.get(callerAddress.getHostAddress()).getSpeakerPort();
                    if (speakerPort != 0) {
                        System.out.println("data sent back to caller: " + callerAddress.getHostAddress() + ":" + speakerPort);
                        udpServerSocket.send(new DatagramPacket(data, 0, data.length, callerAddress, speakerPort));
                    } else {
                        System.out.println("Yet to be connect => No Speaker port available: " + callerAddress.getHostAddress() + ":" + speakerPort);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            session.forEach((ip, client) -> {
                if (!ip.equals(callerAddress.getHostAddress())) { // broadcast packet
                    int destSpeakerPort = session.get(ip).getSpeakerPort();
                    try {
                        if (destSpeakerPort != 0) {
                            System.out.println("data sent to client: " + ip + ":" + destSpeakerPort);
                            udpServerSocket.send(new DatagramPacket(
                                data, 0, data.length, InetAddress.getByName(ip), destSpeakerPort));
                        } else {
                            System.out.println("Yet to be connect => No Speaker port available: " + callerAddress.getHostAddress() + ":" + destSpeakerPort);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    Map<String, Client> session = new HashMap<>();

    private Client createOrGetSession(DatagramPacket receivePacket) {
        Client client = null;
        try {
            int callerPort = receivePacket.getPort();
            InetAddress callerInetAddress = receivePacket.getAddress();
            String callerIp = callerInetAddress.getHostAddress();

            if (session.containsKey(callerIp)) {
                String code = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                if (code.contains(PortType.SENDER.toString())) {
                    client = session.get(callerIp).setPort(callerPort, PortType.SENDER);
                }
                if (code.contains(PortType.RECEIVER.toString())) {
                    client = session.get(callerIp).setPort(callerPort, PortType.RECEIVER);
                }
            } else {
                //StartRecording();
                //System.out.println("StartRecoding...");

                String code = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                if (code.contains(PortType.SENDER.toString())) {
                    client = new Client(callerIp, callerPort, PortType.SENDER);
                }
                if (code.contains(PortType.RECEIVER.toString())) {
                    client = new Client(callerIp, callerPort, PortType.RECEIVER);
                }
                if (client != null) {
                    session.put(callerIp, client);
                    //printAllSession();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    private void printAllSession() {
        session.forEach((key, value) -> {
            System.out.println("ip: " + key + " ,mic: " + value.getMicPort() + " ,speaker: " + value.getSpeakerPort());
        });
    }

    public static void main(String args[]) throws Exception {
        new Main().runVOIP();
    }
}