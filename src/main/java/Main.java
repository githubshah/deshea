
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    final int serverPort = 9786;
    DatagramSocket udpServerSocket;
    ByteArrayOutputStream byteArrayOutputStream;

    public void runVOIP() {
        try {
            udpServerSocket = new DatagramSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[40000];
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            ThreadPoolExecutor executor1 = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(tempBuffer, tempBuffer.length);
                System.out.println("Server waiting to retrieve: " + serverPort);
                udpServerSocket.receive(receivePacket);
                try {
                    System.out.println("write some thing");

                    executor1.execute(() -> {
                        Client session = createOrGetSession(receivePacket);
                    });

                    executor.execute(() -> {
                        sendToClient(receivePacket, tempBuffer);
                    });
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(DatagramPacket receivePacket, byte[] tempBuffer) {
        System.out.println("recoding...");
        //int speakerPort = session.get("132.154.242.243").getSpeakerPort();
        int speakerPort = session.get("127.0.0.1").getSpeakerPort();
        try {
            System.out.println("data sent back to client: "+speakerPort);
            udpServerSocket.send(new DatagramPacket(tempBuffer, 0,
                tempBuffer.length, receivePacket.getAddress(), speakerPort));
        } catch (IOException e) {
            e.printStackTrace();
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
                    printAllSession();
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