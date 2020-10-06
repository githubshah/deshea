package com.roava.video;

import com.roava.audio.UtilAudio;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
        tmrVideoProcess = new Timer(10, new ActionListener() {
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

    private DatagramSocket clientSenderSocket;

    public void setSocket(DatagramSocket clientSenderSocket) {
        this.clientSenderSocket = clientSenderSocket;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }
}
