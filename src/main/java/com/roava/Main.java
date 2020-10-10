package com.roava;

import com.roava.core.WebCam;
import com.roava.video.patient.ReadThread;
import com.roava.video.patient.ImageViewPane;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.Socket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BufferedImage image1 = ImageIO.read(new File("/Users/daffolapmac-156/Desktop/as.png"));
        Image image = SwingFXUtils.toFXImage(image1, null);
        ImageView imageView = new ImageView();
        imageView.setImage(image);

        ImageViewPane viewPane = new ImageViewPane(imageView);

        VBox vbox = new VBox();
        StackPane root = new StackPane();
        root.getChildren().addAll(viewPane);

        vbox.getChildren().addAll(root);
        VBox.setVgrow(root, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 1600, 1200);
        primaryStage.setScene(scene);

        primaryStage.setMaxHeight(800);
        primaryStage.setMinHeight(200);
        primaryStage.setMaxWidth(1200);
        primaryStage.setMinWidth(200);

        primaryStage.setTitle("patient");
        primaryStage.show();

        Socket socket = new Socket("127.0.0.1", 9898);
        //Socket socket1 = new Socket("127.0.0.1", 9898);
        System.out.println("Connected to the chat server");
        WebCam webCam = new WebCam();
        webCam.populateInSocket(socket);
        webCam.start();
        new ReadThread(socket, imageView).start();

    }

    public static void main(String[] args) {
        System.out.printf("java.library.path: %s%n", System.getProperty("java.library.path"));
        nu.pattern.OpenCV.loadShared(); //add this
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
        System.loadLibrary("opencv_java320");

        launch(args);
    }
}
