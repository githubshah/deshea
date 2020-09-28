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
import java.net.SocketException;

class ReceiverThread extends Thread {

    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private SourceDataLine sourceLine;
    private AudioInputStream audioInputStream;

    public ReceiverThread(DatagramSocket ds) throws SocketException, LineUnavailableException {
        System.out.println("Receiver Thread Connected to the server "
            + ds.getInetAddress().getHostAddress() + ":" + ds.getPort());
        this.udpClientSocket = ds;
        System.out.println("Sender Thread Opened Speaker");
        this.openSpeaker();
    }

    public void halt() {
        this.stopped = true;
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public void run() {
        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[4096];
        while (true) {
            if (stopped) { // todo
                System.out.println("Call dropped");
                return;
            }
            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // Receive a packet from the server (blocks until the packets are received)
                udpClientSocket.setSoTimeout(10000); // timeout 10 sec
                System.out.println("waiting to receive data from server...");
                udpClientSocket.receive(receivePacket);
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