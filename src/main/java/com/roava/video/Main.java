package com.roava.video;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main extends Application {

    static ImageView imageView;

    @Override
    public void start(Stage primaryStage) throws Exception {


        start();


        primaryStage.setTitle("ImageView Experiment 1");

        FileInputStream input = new FileInputStream("/Users/daffolapmac-156/Desktop/sas.png");

        Image image = new Image(input);
        imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        HBox hbox = new HBox(imageView);

        Scene scene = new Scene(hbox, 800, 800);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {

//        System.out.printf("java.library.path: %s%n", System.getProperty("java.library.path"));
//        nu.pattern.OpenCV.loadShared(); //add this
//        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
//        System.out.println("mat = " + mat.dump());
//        System.loadLibrary("opencv_java320");

        Application.launch(args);
    }

    //region Properties
    public static Mat frame = null;
    static VideoCapture videoCapture;
    static Timer tmrVideoProcess;
    //endregion

    //region Methods

    public static void start() {

        new Thread(() -> {
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
                        byte[] imageBytes = baos.toByteArray();
                        Image image = SwingFXUtils.toFXImage(img, null);
                        imageView.setImage(image);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
            tmrVideoProcess.start();
        }).start();
    }

    public static BufferedImage Mat2bufferedImage(Mat image) throws IOException {
        MatOfByte bytemat = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        img = ImageIO.read(in);
        //img.se
        return img;
    }
}
