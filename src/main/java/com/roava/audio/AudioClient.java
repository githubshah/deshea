package com.roava.audio;

import com.roava.config.ServerSetting;

import javax.sound.sampled.LineUnavailableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class AudioClient {
    private String host;
    private int clientPort;

    public AudioClient(String host, int port) {
        this.host = host;
        this.clientPort = port;
    }

    public void openChannel() throws UnknownHostException, SocketException, LineUnavailableException {
        String host = ServerSetting.SERVER_IP;
        int clientPort = ServerSetting.SERVER_PORT;

        // Get the port number to use from the command line
        System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientPort);

        // Get the IP address of the local machine - we will use this as the address to send the data to
        InetAddress ia = InetAddress.getByName(host);
        AudioSenderThread sender = new AudioSenderThread(ia, clientPort);
        sender.start();
        AudioReceiverThread receiver = new AudioReceiverThread(ia, clientPort);
        receiver.start();
    }
}