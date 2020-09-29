package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class ReceiverThread extends Thread {

    private DatagramSocket clientReceiverSocket;
    private boolean stopped = false;
    private SourceDataLine sourceLine;
    private AudioInputStream audioInputStream;
    private int serverPort;
    private InetAddress serverIPAddress;

    public ReceiverThread(InetAddress serverIPAddress, int serverPort) throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientReceiverSocket = new DatagramSocket();
        this.clientReceiverSocket.connect(serverIPAddress, serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        try {
            byte[] sendData = new byte[10];
            sendData = "RECEIVER".getBytes( StandardCharsets.UTF_8 );
            clientReceiverSocket.send(new DatagramPacket(sendData,sendData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.openSpeaker();
        System.out.println("Sender Thread Opened Speaker");
    }

    public void halt() {
        this.stopped = true;
    }

    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 44100.0f;
        int channels = 2;
        int frameSize = 4;
        int sampleSize = 16;
        boolean bigEndian = true;

        return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
            * channels, rate, bigEndian);
    }

    public void run() {
        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[1024];
        while (true) {
            if (stopped) { // todo
                System.out.println("Call dropped");
                return;
            }
            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // Receive a packet from the server (blocks until the packets are received)
                clientReceiverSocket.setSoTimeout(10000); // timeout 10 sec
                System.out.println("waiting to receive data from server...");
                clientReceiverSocket.receive(receivePacket);
                try {
                    InputStream byteInputStream = new ByteArrayInputStream(receivePacket.getData());
                    audioInputStream = new AudioInputStream(byteInputStream, getAudioFormat(), receivePacket.getData().length / getAudioFormat().getFrameSize());
                    Thread t = new Thread(new PlayThread(sourceLine, audioInputStream));
                    t.start();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
            Thread.yield();
        }
    }

    private void openSpeaker() throws LineUnavailableException {
        AudioFormat adFormat = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
        sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceLine.open(adFormat);
        sourceLine.start();
    }
}