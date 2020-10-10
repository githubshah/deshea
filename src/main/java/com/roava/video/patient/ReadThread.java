package com.roava.video.patient;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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
    private ImageView imageView;

    public ReadThread(Socket socket, ImageView imageView) {
        this.socket = socket;
        this.imageView = imageView;

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
                imageView.setImage(image);
            } catch (IOException ex) {
                System.out.println("Error in chat.UserThread: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}