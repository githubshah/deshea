package com.roava.audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class SenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket clientSenderSocket;
    private boolean stopped = false;
    private int serverPort;
    private TargetDataLine micLine;

    //InetAddress IPAddress = InetAddress.getByName("192.168.1.3");
    public SenderThread(InetAddress serverIPAddress, int serverPort)
        throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientSenderSocket = new DatagramSocket();
        this.clientSenderSocket.connect(serverIPAddress, serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        this.micLine = new Mic().openMicLine();
        System.out.println("Sender Thread Opened Mic");
    }

    public void halt() {
        this.stopped = true;
    }

    public DatagramSocket getSocket() {
        return this.clientSenderSocket;
    }

    byte tempBuffer[] = new byte[4096];

    public void run() {

        if (this.micLine == null) {
            System.out.println("LineUnavailableException unavailable");
            return;
        }

        if (this.clientSenderSocket == null) {
            System.out.println("clientSenderSocket  unavailable");
            return;
        }

        boolean stopaudioCapture = false;
        try {
            int cnt;
            while (!stopaudioCapture) {
                cnt = micLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, serverIPAddress, serverPort);
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