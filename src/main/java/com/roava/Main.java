package com.roava;

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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        BufferedImage image1 = ImageIO.read(
            new File("/Users/daffolapmac-156/shaid-data/DesheaPatient/src/main/resources/image/ThankYou.png"));
        Image image = SwingFXUtils.toFXImage(image1, null);
        ImageView imageView = new ImageView();
        imageView.setImage(image);

        ImageViewPane viewPane = new ImageViewPane(imageView);

        VBox vbox = new VBox();
        StackPane root = new StackPane();
        root.getChildren().addAll(viewPane);

        vbox.getChildren().addAll(root);
        VBox.setVgrow(root, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);

        primaryStage.setMaxHeight(800);
        primaryStage.setMinHeight(200);
        primaryStage.setMaxWidth(1200);
        primaryStage.setMinWidth(200);

        primaryStage.setTitle("patient");
        primaryStage.show();

        SocketBridge socketBridge = new SocketBridge();
        socketBridge.renderIn(imageView);
        socketBridge.connectToServer();
        //socketBridge.disconnectFromServer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
