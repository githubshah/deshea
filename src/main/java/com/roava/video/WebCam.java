package com.roava.video;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class WebCam extends Thread {
    private static Mat frame = null;
    private VideoCapture videoCapture;
    private Timer tmrVideoProcess;

    private ImageView imageView;

    @Override
    public void run() {
        videoCapture = new VideoCapture();
        videoCapture.open(0);
        if (!videoCapture.isOpened()) {
            return;
        }

        frame = new Mat();
        tmrVideoProcess = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!videoCapture.read(frame)) {
                    tmrVideoProcess.stop();
                }

                //procesed image => frame
                BufferedImage img = null;
                try {
                    img = Mat2bufferedImage(frame);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "jpg", baos);
                    Image image = SwingFXUtils.toFXImage(img, null);
                    if (imageView != null) {
                        imageView.setImage(image);
                    }
                    if (dout != null) {
                        System.out.println("Ready to Send picture...");
                        BufferedImage bImage = null;
                        try {
                            byte[] data = baos.toByteArray();
                            dout.writeInt(data.length);
                            if (data.length > 0) {
                                dout.write(data, 0, data.length);
                                System.out.println("Sent picture...");
                            }
                        } catch (Exception exx) {
                            exx.printStackTrace();
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        tmrVideoProcess.start();
    }

    public BufferedImage Mat2bufferedImage(Mat image) throws IOException {
        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        img = ImageIO.read(in);
        return img;
    }

    public void populateIn(ImageView imageView) {
        this.imageView = imageView;
    }

    Socket socket;
    private DataOutputStream dout;

    public void populateInSocket(Socket socket) {
        this.socket = socket;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}