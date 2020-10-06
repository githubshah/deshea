package com.roava.video;

import javafx.scene.image.ImageView;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class VideoSenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket clientSenderSocket;
    private int serverPort;
    private TargetDataLine targetDataLine;
    private ImageView loggedUser;

    public VideoSenderThread(InetAddress serverIPAddress, int serverPort, ImageView loggedUser)
        throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientSenderSocket = new DatagramSocket();
        this.clientSenderSocket.connect(serverIPAddress, serverPort);
        // connect to server
        this.requestToConnect();
        this.loggedUser = loggedUser;
    }

    private void requestToConnect() {
        try {
            byte[] sendData = new byte[10];
            sendData = "SENDER".getBytes(StandardCharsets.UTF_8);
            clientSenderSocket.send(new DatagramPacket(sendData, sendData.length));
            System.out.println("Sender Thread Connected to the server "
                + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        WebCam webCam = new WebCam();
        webCam.setImageView(loggedUser);
        webCam.setSocket(clientSenderSocket);
        webCam.start();
    }
}