package com.roava.audio;

import com.roava.config.ServerSetting;

import javax.sound.sampled.LineUnavailableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
    private String host;
    private int clientPort;

    public Client(String host, int port) {
        this.host = host;
        this.clientPort = port;
    }

    public static void main(String args[]) throws Exception {
        // The default port
        int clientport = 9786;
        String host = "34.66.62.184";
        //String host = "localhost";
        new Client(host, clientport).openChannel();
    }

    ReceiverThread receiver;

    public void openChannel() throws UnknownHostException, SocketException, LineUnavailableException {
        String host = ServerSetting.SERVER_IP;
        int clientPort = ServerSetting.SERVER_PORT;

        // Get the port number to use from the command line
        System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientPort);

        // Get the IP address of the local machine - we will use this as the address to send the data to
        InetAddress ia = InetAddress.getByName(host);
        SenderThread sender = new SenderThread(ia, clientPort);
        sender.start();
        receiver = new ReceiverThread(ia, clientPort);
        receiver.start();
    }
}