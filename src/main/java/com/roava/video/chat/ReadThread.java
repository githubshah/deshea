package com.roava.video.chat;

import com.roava.video.VideoClient;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
 
/**
 * This thread is responsible for reading server's input and printing it
 * to the console.
 * It runs in an infinite loop until the client disconnects from the server.
 *
 * @author www.codejava.net
 */
public class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private VideoClient client;
 
    public ReadThread(Socket socket, VideoClient client) {
        this.socket = socket;
        this.client = client;
 
        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
 
    public void run() {
        while (true) {
            try {
                DataInputStream din = new DataInputStream(socket.getInputStream());
                int len = din.readInt();
                byte[] data = new byte[len];
                if (len > 0) {
                    din.readFully(data);
                }

                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                BufferedImage bImage2 = ImageIO.read(bis);
                Image image = SwingFXUtils.toFXImage(bImage2, null);
                //ImageIO.write(bImage2, "png", new File("output" + Math.random() + ".png"));
                client.getRemoteUser().setImage(image);
            } catch (IOException ex) {
                System.out.println("Error in chat.UserThread: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}