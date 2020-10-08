package com.roava.video;

import com.roava.config.ServerSetting;
import com.roava.video.chat.ReadThread;
import javafx.scene.image.ImageView;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.Socket;

public class VideoClient {
    private String host;
    private int clientPort;

    public ImageView getImageView() {
        return imageView;
    }

    ImageView imageView;

    ImageView remoteUser;

    public VideoClient(String host, int port, ImageView imageView, ImageView remoteUser) {
        this.host = host;
        this.clientPort = port;
        this.imageView = imageView;
        this.remoteUser = remoteUser;
    }

    public void openChannel() throws IOException, LineUnavailableException {
        String host = ServerSetting.SERVER_IP;
        int clientPort = ServerSetting.SERVER_PORT;


        WebCam webCam = new WebCam();
        webCam.populateIn(imageView);
        webCam.start();

        /*Socket socket = new Socket("3.16.216.89", 9898);
        //Socket socket = new Socket("127.0.0.1", 9898);
        webCam.populateInSocket(socket); // write thread

        new ReadThread(socket, this).start();*/
    }

    public ImageView getRemoteUser() {
        return remoteUser;
    }
}