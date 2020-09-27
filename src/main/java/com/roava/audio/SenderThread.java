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
    private int serverport;
    TargetDataLine targetDataLine;

    //InetAddress IPAddress = InetAddress.getByName("192.168.1.3");
    public SenderThread(InetAddress address, int serverport)
        throws SocketException, LineUnavailableException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.clientSenderSocket = new DatagramSocket();
        this.clientSenderSocket.connect(serverIPAddress, serverport);
        targetDataLine = new Mic().getTargetDataLine();
    }

    public void halt() {
        this.stopped = true;
    }

    public DatagramSocket getSocket() {
        return this.clientSenderSocket;
    }

    byte tempBuffer[] = new byte[4096];

    public void run() {

        if (this.targetDataLine == null) {
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
            System.out.println("serverIPAddress: "+serverIPAddress);
            System.out.println("serverport: "+serverport);
            while (!stopaudioCapture) {
                cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, serverIPAddress, serverport);
                    clientSenderSocket.send(sendPacket);
                    Thread.yield();
                }
            }
        } catch (Exception e) {
            System.out.println("CaptureThread::run()" + e);
            System.exit(0);
        }
    }
}   