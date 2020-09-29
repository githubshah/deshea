
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    enum TYPE {
        SENDER, RECEIVER;
    }

    final int serverPort = 9786;
    int dataPacketSize = 1024;
    ByteArrayOutputStream byteOutputStream;
    AudioFormat adFormat;
    TargetDataLine targetDataLine;
    AudioInputStream inputStream;
    SourceDataLine sourceLine;
    Map<String, Integer> connectedClientsPortMap = new ConcurrentHashMap<>(); // [ip:port , port]
    Map<String, String> connectedClientsIpMap = new ConcurrentHashMap(); // [ip:port , ip]
    Map<String, Map<TYPE, Integer>> ipSenderReceiverType_Port = new ConcurrentHashMap();  //[ip, [type, port]]
    Map<String, Map<Integer, TYPE>> ipSenderReceiverPort_Type = new ConcurrentHashMap();  //[ip, [port, type]]


    ExecutorService executor = Executors.newFixedThreadPool(100);//


    Map<String, String> roomToFrom = new HashMap<>();
    Map<String, String> roomFromTo = new HashMap<>();

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
        String shaidIp = "47.31.148.71";
        String shaidIp2 = "132.154.66.61";
        //String shaidIp3 = "47.31.148.71";
        roomToFrom.put(shaidIp, shaidIp2);
        roomFromTo.put(shaidIp2, shaidIp);

        try {
            udpServerSocket = new DatagramSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void connect() throws IOException {
        while (true) {
            byte[] receiveData = new byte[dataPacketSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpServerSocket.receive(receivePacket);
            try {
                int callerPort = receivePacket.getPort();
                InetAddress callerInetAddress = receivePacket.getAddress();
                String callerIp = callerInetAddress.getHostAddress();

                String kKey = getKKey(callerIp, callerPort);

                System.out.println(kKey);
                //System.out.println("get Key " + connectedClientsPortMap.get(kKey));
                if (!connectedClientsPortMap.containsKey(kKey)) {
                    System.out.println("New Client Connected : " +
                        callerIp + ":" + receivePacket.getPort());
                    String code = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                    System.out.println("serverReply: " + code);
                    connectedClientsPortMap.put(kKey, callerPort);
                    connectedClientsIpMap.put(kKey, callerIp);

                    if (code.contains(TYPE.SENDER.toString())) {
                        if (ipSenderReceiverType_Port.containsKey(callerIp)) {
                            System.out.println("1: " + code);
                            ipSenderReceiverType_Port.get(callerIp).put(TYPE.SENDER, callerPort);
                            ipSenderReceiverPort_Type.get(callerIp).put(callerPort, TYPE.SENDER);
                        } else {
                            System.out.println("2: " + code);
                            Map<TYPE, Integer> ipPort = new HashMap<>();
                            ipPort.put(TYPE.SENDER, callerPort);
                            ipSenderReceiverType_Port.put(callerIp, ipPort);

                            Map<Integer, TYPE> ipType = new HashMap<>();
                            ipType.put(callerPort, TYPE.SENDER);
                            ipSenderReceiverPort_Type.put(callerIp, ipType);
                        }
                    }
                    if (code.contains(TYPE.RECEIVER.toString())) {
                        if (ipSenderReceiverType_Port.containsKey(callerIp)) {
                            System.out.println("3: " + code);
                            ipSenderReceiverType_Port.get(callerIp).put(TYPE.RECEIVER, callerPort);
                            ipSenderReceiverPort_Type.get(callerIp).put(callerPort, TYPE.RECEIVER);
                        } else {
                            System.out.println("4: " + code);
                            Map<TYPE, Integer> ipPort = new HashMap<>();
                            ipPort.put(TYPE.RECEIVER, callerPort);
                            ipSenderReceiverType_Port.put(callerIp, ipPort);

                            Map<Integer, TYPE> ipType = new HashMap<>();
                            ipType.put(callerPort, TYPE.RECEIVER);
                            ipSenderReceiverPort_Type.put(callerIp, ipType);
                        }
                    }
                    Thread.sleep(200);

                    ipSenderReceiverPort_Type.forEach((ip, m) -> {
                        m.forEach((type, port) -> {
                            System.out.println(ip + " : " + port + " : " + type);
                        });
                    });
                }

                this.sendToClient(connectedClientsIpMap, connectedClientsPortMap,
                    receivePacket, callerIp, callerPort);
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    private String getKKey(String callerIp, int callerPort) {
        return callerIp + ":" + callerPort;
    }

    /**
     * @param connectedClientsIpMap
     * @param connectedClientsMap   : [callerIp,callerPort]
     *                              //* @param audioData
     * @param callerIp              :            caller ip
     * @param callerPort            :          caller port
     */
    private void sendToClient(Map<String, String> connectedClientsIpMap, Map<String, Integer> connectedClientsMap, DatagramPacket receivePacket, String callerIp, int callerPort) {

        /*
         * 1. get user from its room i.e ip of connect user
         * 2. get type of packet. i.e sender or receiver note 99.9% will be sender
         * 3. if(sender) => get connected ip and receiver port
         * 4. if(receiver) => get connected ip and sender port
         * */

        String destinationUserIp = roomToFrom.get(callerIp);
        if (destinationUserIp == null) {
            destinationUserIp = roomFromTo.get(callerIp);
        }

        if (destinationUserIp != null && ipSenderReceiverType_Port.get(destinationUserIp) != null && !callerIp.equals(destinationUserIp)) {
            int destinationUserPort = ipSenderReceiverType_Port.get(destinationUserIp).get(TYPE.RECEIVER);
            System.out.println(callerIp + ":" + callerPort + " => " + destinationUserIp + ":" + destinationUserPort);

            try {
                System.out.println("Packet send to ip: " + destinationUserIp + ", port: " + destinationUserPort);
                byte[] data = receivePacket.getData();
                DatagramPacket sendPacket =
                    new DatagramPacket(data, data.length, InetAddress.getByName(destinationUserIp), destinationUserPort);
                //Thread.yield();
                executor.submit(()->{
                    Thread.yield();
                    try {
                        udpServerSocket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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