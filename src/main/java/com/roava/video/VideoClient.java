package com.roava.video;

import com.roava.config.ServerSetting;
import javafx.scene.image.ImageView;

import javax.sound.sampled.LineUnavailableException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VideoClient {
    private String host;
    private int clientPort;
    ImageView loggedUser;

    public VideoClient(String host, int port, ImageView loggedUser) {
        this.host = host;
        this.clientPort = port;
        this.loggedUser = loggedUser;
    }

    public void openChannel() throws UnknownHostException, SocketException, LineUnavailableException {
        String host = ServerSetting.SERVER_IP;
        int clientPort = ServerSetting.SERVER_PORT;

        // Get the port number to use from the command line
        System.out.println("Usage: UDPClient " + "Now using host = " + host + ", Port# = " + clientPort);

        // Get the IP address of the local machine - we will use this as the address to send the data to
        InetAddress ia = InetAddress.getByName(host);

        VideoSenderThread sender = new VideoSenderThread(ia, clientPort, loggedUser);
        sender.start();
        VideoReceiverThread receiver = new VideoReceiverThread(ia, clientPort);
        receiver.start();
    }
}