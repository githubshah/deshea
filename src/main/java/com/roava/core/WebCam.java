package com.roava.core;

import org.imgscalr.Scalr;
import org.opencv.core.CvType;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/* how to use..
*
        WebCam webCam = new WebCam();
        webCam.populateIn(imageView);
        webCam.setSocket(clientSenderSocket);
        webCam.start();
*
* */
public class WebCam extends Thread {
    private static Mat frame = null;
    private VideoCapture videoCapture;
    private Timer tmrVideoProcess;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }

    @Override
    public void run() {
        videoCapture = new VideoCapture();
        videoCapture.open(0);
        if (!videoCapture.isOpened()) {
            return;
        }

        frame = new Mat();
        tmrVideoProcess = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!videoCapture.read(frame)) {
                    tmrVideoProcess.stop();
                }

                //procesed image => frame
                BufferedImage img = null;
                try {
                    img = Mat2bufferedImage(frame);

                    try {
                        // img = resizeImage(img, WIDTH, HEIGHT);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }


                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "jpg", baos);

                    if (dout != null) {
                        System.out.println("Ready to Send picture...");
                        executor.execute(() -> {
                            try {
                                byte[] data = baos.toByteArray();
                                dout.writeInt(data.length);
                                if (data.length > 0) {
                                    dout.write(data, 0, data.length);
                                    System.out.println("Sent picture...");
                                    baos.flush();
                                }
                            } catch (Exception exx) {
                                exx.printStackTrace();
                            }
                        });
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

    private Socket socket;
    private DataOutputStream dout;
    private int WIDTH = 300;
    private int HEIGHT = 300;

    public void populateInSocket(Socket socket) {
        this.socket = socket;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setWebCamScreen(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
    }

    public WebCam(){
        System.out.printf("load java.library.path: %s%n", System.getProperty("java.library.path"));
        nu.pattern.OpenCV.loadShared(); //add this
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
        System.loadLibrary("opencv_java320");
    }
}