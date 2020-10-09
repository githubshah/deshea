package com.roava.video.patient;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizer extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
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

        Scene scene = new Scene(vbox, 1200, 800);
        primaryStage.setScene(scene);

        primaryStage.setMaxHeight(800);
        primaryStage.setMinHeight(200);
        primaryStage.setMaxWidth(1200);
        primaryStage.setMinWidth(200);

        primaryStage.setTitle("patient");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */


/**
 * @author akouznet
 */
class ImageViewPane extends Region {

    private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();

    public ObjectProperty<ImageView> imageViewProperty() {
        return imageViewProperty;
    }

    public ImageView getImageView() {
        return imageViewProperty.get();
    }

    public void setImageView(ImageView imageView) {
        this.imageViewProperty.set(imageView);
    }

    public ImageViewPane() {
        this(new ImageView());
    }

    @Override
    protected void layoutChildren() {
        ImageView imageView = imageViewProperty.get();
        if (imageView != null) {
            imageView.setFitWidth(getWidth());
            imageView.setFitHeight(getHeight());
            layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
        super.layoutChildren();
    }

    public ImageViewPane(ImageView imageView) {
        imageViewProperty.addListener(new ChangeListener<ImageView>() {

            @Override
            public void changed(ObservableValue<? extends ImageView> arg0, ImageView oldIV, ImageView newIV) {
                if (oldIV != null) {
                    getChildren().remove(oldIV);
                }
                if (newIV != null) {
                    getChildren().add(newIV);
                }
            }
        });
        this.imageViewProperty.set(imageView);
    }
}