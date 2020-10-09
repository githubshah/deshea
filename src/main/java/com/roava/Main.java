package com.roava;

import com.roava.controller.Controller;
import com.roava.video.WebCam;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.text.html.ImageView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/video.fxml"));
        Parent root = (Parent)fxmlLoader.load();
        Controller controller = fxmlLoader.<Controller>getController();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1035, 721));
        primaryStage.show();

//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/video.fxml"));
//        Parent root = (Parent)fxmlLoader.load();
//        Controller controller = fxmlLoader.<Controller>getController();
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 1035, 721));
//        primaryStage.show();

    }

    public static void main(String[] args)
    {
        System.out.printf("java.library.path: %s%n", System.getProperty("java.library.path"));
        nu.pattern.OpenCV.loadShared(); //add this
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
        System.loadLibrary("opencv_java320");

        launch(args);
    }
}
