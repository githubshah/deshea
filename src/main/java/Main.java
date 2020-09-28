
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {

    final int serverPort = 9786;
    int dataPacketSize = 4096;
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream inputStream;
    SourceDataLine sourceLine;
    Map<String, Integer> connectedClientsPortMap = new HashMap(); // [ip:port , port]
    Map<String, String> connectedClientsIpMap = new HashMap(); // [ip:port , ip]

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    DatagramSocket udpServerSocket;

    public void runVOIP() {
        try {
            udpServerSocket = new DatagramSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
            while (true) {
                byte[] receiveData = new byte[dataPacketSize];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                udpServerSocket.receive(receivePacket);
                try {
                    int callerPort = receivePacket.getPort();
                    InetAddress callerInetAddress = receivePacket.getAddress();
                    String callerIp = callerInetAddress.getHostAddress();

                    String kKey = getKKey(callerIp, callerPort);

                    if (!connectedClientsPortMap.containsKey(kKey)) {
                        System.out.println("New Client Connected : " +
                            callerIp + ":" + receivePacket.getPort());
                        String code =  new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("serverReply: "+code);
                        connectedClientsPortMap.put(callerIp, callerPort);
                        connectedClientsIpMap.put(callerIp, callerIp);
                    }

                    new Thread(() -> {
                        this.sendToClient(connectedClientsIpMap, connectedClientsPortMap,
                            receivePacket, callerIp, callerPort);
                    }).start();
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getKKey(String callerIp, int callerPort) {
        return callerIp + ":" + callerPort;
    }

    /**
     * @param connectedClientsIpMap
     * @param connectedClientsMap : [callerIp,callerPort]
     *                             //* @param audioData
     * @param callerIp :            caller ip
     * @param callerPort :          caller port
     */
    private void sendToClient(Map<String, String> connectedClientsIpMap, Map<String, Integer> connectedClientsMap, DatagramPacket receivePacket, String callerIp, int callerPort) {
        System.out.println("Connected User count: " + connectedClientsMap.size());
//        connectedClientsMap.forEach((connectedUserIp, connectedUserPort) -> {
//            try {
//                if (!connectedUserIp.equals(callerIp)) { // skip local host client
//                    Thread.yield();
//                    System.out.println("Packet send to ip: " + connectedUserIp + ", port: " + connectedUserPort);
//                    DatagramPacket sendPacket =
//                        new DatagramPacket(receivePacket.getData(),
//                            receivePacket.getData().length, InetAddress.getByName(connectedUserIp), connectedUserPort);
//                    udpServerSocket.send(sendPacket);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    private void playHere(byte[] audioData) throws LineUnavailableException {
        InputStream byteInputStream = new ByteArrayInputStream(audioData);
        AudioFormat adFormat = getAudioFormat();
        inputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
        sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceLine.open(adFormat);
        sourceLine.start();
        Thread playThread = new Thread(new PlayThread());
        playThread.start();
    }

    public class PlayThread extends Thread {

        byte tempBuffer[] = new byte[dataPacketSize];

        public void run() {
            try {
                int cnt;
                while ((cnt = inputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        sourceLine.write(tempBuffer, 0, cnt);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    private static HashSet<Integer> portSet = new HashSet<Integer>();

    public static void main(String args[]) throws Exception {
        new Main().runVOIP();
    }
}