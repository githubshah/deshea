package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class SenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket clientSenderSocket;
    private boolean stopped = false;
    private int serverPort;
    private TargetDataLine micLine;

    public SenderThread(InetAddress serverIPAddress, int serverPort)
        throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientSenderSocket = new DatagramSocket();
        this.clientSenderSocket.connect(serverIPAddress, serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        try {
            byte[] sendData = new byte[10];
            sendData = "SENDER".getBytes( StandardCharsets.UTF_8 );
            clientSenderSocket.send(new DatagramPacket(sendData,sendData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.micLine = new Mic().openMicLine();
        System.out.println("Sender Thread Opened Mic");
    }

    public void halt() {
        this.stopped = true;
    }

    public DatagramSocket getSocket() {
        return this.clientSenderSocket;
    }



    public void run() {
        byte tempBuffer[] = new byte[micLine.getBufferSize() / 5];

        if (this.micLine == null) {
            System.out.println("LineUnavailableException unavailable");
            return;
        }

        if (this.clientSenderSocket == null) {
            System.out.println("clientSenderSocket  unavailable");
            return;
        }

        boolean stopaudioCapture = false;

        // play back the captured audio data
        int frameSizeInBytes = getAudioFormat().getFrameSize();
        int bufferLengthInFrames = micLine.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];

        try {
            int cnt;
            while (!stopaudioCapture) {
                cnt = micLine.read(data, 0, bufferLengthInBytes);
                if (cnt > 0) {
                    DatagramPacket sendPacket = new DatagramPacket(data, bufferLengthInBytes, serverIPAddress, serverPort);
                    clientSenderSocket.send(sendPacket);
                    System.out.println(">>>>>> mic to server: " + serverIPAddress.getHostAddress() + ":" + serverPort);
                    Thread.yield();
                }
            }
        } catch (Exception e) {
            System.out.println("CaptureThread::run()" + e);
            System.exit(0);
        }
    }
}   