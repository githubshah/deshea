package com.roava.video.chat;

import com.roava.video.VideoClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * This thread is responsible for reading user's input and send it
 * to the server.
 * It runs in an infinite loop until the user types 'bye' to quit.
 *
 * @author www.codejava.net
 */
public class WriteThread extends Thread {
    private DataOutputStream dout;
    private Socket socket;
    private ChatClient client;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            System.out.println("Send picture...");
            BufferedImage bImage = null;
            try {
                bImage = ImageIO.read(new File("/Users/daffolapmac-156/Downloads/image/caller.png"));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", bos);
                byte[] data = bos.toByteArray();
                dout.writeInt(data.length);
                if (data.length > 0) {
                    dout.write(data, 0, data.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}