package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
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

    public ReceiverThread(DatagramSocket ds) throws SocketException {
        System.out.println("port: " + ds.getPort());
        System.out.println("ip: " + ds.getInetAddress());
        this.udpClientSocket = ds;
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
        AudioFormat adFormat = getAudioFormat();
        while (true) {
            if (stopped) { // todo
                System.out.println("Call dropped");
                return;
            }

            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //System.out.println("rec length: "+receivePacket.getLength());

            try {
                // Receive a packet from the server (blocks until the packets are received)
                System.out.println("PRE RECEIVED: ");
                udpClientSocket.setSoTimeout(2000); // timeout 10 sec
                udpClientSocket.receive(receivePacket);
                System.out.println("RECEIVED: ");
                try {
                    byte audioData[] = receivePacket.getData();
                    System.out.println("audio data: " + audioData.length);
                    InputStream byteInputStream = new ByteArrayInputStream(audioData);
                    adFormat = getAudioFormat();
                    audioInputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
                    sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceLine.open(adFormat);
                    sourceLine.start();
                    Thread t = new Thread(new PlayThread(sourceLine, audioInputStream));
                    t.start();
                    System.out.println("2RECEIVED: ");
                } catch (Exception e) {
                    e.getStackTrace();
                }

                // print to the screen
                //System.out.println("kohli UDPClient: Response from Server: \"" + serverReply + "\"\n");

                Thread.yield();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}