package com.roava.controller;

import com.roava.audio.Client;
import com.roava.config.ServerSetting;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
//https://www.daniweb.com/programming/software-development/threads/392710/basic-udp-chat-system
public class Controller implements Initializable {



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init");
    }

    @FXML
    void callOn(){
        System.out.println("callOn");
        try {
            new Client(ServerSetting.SERVER_IP, ServerSetting.SERVER_PORT).openChannel();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void callOff(){
        System.out.println("callOff");
    }
}
