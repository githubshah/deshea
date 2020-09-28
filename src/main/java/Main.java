
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
import java.net.UnknownHostException;
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
    Map<InetAddress, Integer> connectedClients = new HashMap();

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
            System.out.println("Server started...\n");
            System.out.println("on port: " + serverPort);
            while (true) {
                byte[] receiveData = new byte[dataPacketSize];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                udpServerSocket.receive(receivePacket);
                System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
                try {
                    byte[] audioData = new byte[4096];
                    audioData = receivePacket.getData();
                    int clientport = receivePacket.getPort();

                    InetAddress clientIpAddress = receivePacket.getAddress();
                    System.out.println("receivePacket: " + clientIpAddress);
                    connectedClients.put(clientIpAddress, clientport);

                    //this.playHere(audioData);
                    this.sendToClient(connectedClients, audioData, clientIpAddress);
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(Map<InetAddress, Integer> connectedClients, byte[] sendData, InetAddress caller) throws UnknownHostException {
        System.out.println("Send Packet to Client...");
        //InetAddress iPAddress = InetAddress.getByName("192.168.1.3");
        /*connectedClients.forEach((connectedUser, clientport) -> {
            try {
                if (!connectedUser.getHostAddress().equals(caller.getHostAddress())) { // skip local host client
                    System.out.println("");
                    System.out.println("Packet send to ip: " + connectedUser.getHostAddress());
                    System.out.println("Packet send to port: " + clientport);
                    System.out.println();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, connectedUser, clientport);
                    udpServerSocket.send(sendPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/

        if(caller.getHostAddress().equals("117.253.22.147")){ // 117.253.22.147 23533
            try {
                InetAddress neerajIp = InetAddress.getByName("171.60.172.211");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, neerajIp, 58788);
                udpServerSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                InetAddress shaidIp = InetAddress.getByName("117.253.22.147");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, shaidIp, 23533);
                udpServerSocket.send(sendPacket);
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