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
        //int clientport = 9786;
        //String host = "192.168.1.5";
        //String host = "localhost";
        //openChannal();
    }

    ReceiverThread receiver;

    public void openChannel() throws UnknownHostException, SocketException, LineUnavailableException {
        String host = ServerSetting.SERVER_IP;
        int clientport = ServerSetting.SERVER_PORT;

        // Get the port number to use from the command line
        System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientport);

        // Get the IP address of the local machine - we will use this as the address to send the data to
        InetAddress ia = InetAddress.getByName(host);
        SenderThread sender = new SenderThread(ia, clientport);
        sender.start();
        receiver = new ReceiverThread(sender.getSocket());
        receiver.start();
    }

    public void closeChannel() {
        if (receiver != null) {
            receiver.halt();
        }
    }
}