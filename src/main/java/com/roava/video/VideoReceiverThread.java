package com.roava.video;

import com.roava.audio.UtilAudio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class VideoReceiverThread extends Thread {

    private DatagramSocket clientReceiverSocket;
    private boolean stopped = false;
    private SourceDataLine sourceDataLine;
    private AudioInputStream audioInputStream;
    private int serverPort;
    private InetAddress serverIPAddress;

    public VideoReceiverThread(InetAddress serverIPAddress, int serverPort) throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientReceiverSocket = new DatagramSocket();
        this.clientReceiverSocket.connect(serverIPAddress, serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        try {
            byte[] sendData = new byte[10];
            sendData = "RECEIVER".getBytes(StandardCharsets.UTF_8);
            clientReceiverSocket.send(new DatagramPacket(sendData, sendData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ByteArrayOutputStream byteArrayOutputStream;

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Override
    public void run() {
        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[200000];
        while (true) {
            if (stopped) { // todo
                return;
            }
            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // Receive a packet from the server (blocks until the packets are received)
                //clientReceiverSocket.setSoTimeout(10000); // timeout 10 sec
                System.out.println("waiting to receive data from server...");
                try{
                    clientReceiverSocket.setSoTimeout(5000);
                    clientReceiverSocket.receive(receivePacket);
                    System.out.println("received.. data");
                }catch (Exception e){
                    System.out.println("drop waiting...");
                    continue;
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Thread.yield();
        }
    }
}