package com.roava.controller;

import com.roava.config.ServerSetting;
import com.roava.video.VideoClient;
import com.roava.video.chat.ChatClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import javax.sound.sampled.LineUnavailableException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

//https://www.daniweb.com/programming/software-development/threads/392710/basic-udp-chat-system
public class Controller implements Initializable {


    @FXML
    ImageView loggedUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init");
    }

    @FXML
    void callOn() {
        System.out.println("callOn");
        try {
            new VideoClient(ServerSetting.SERVER_IP, ServerSetting.SERVER_PORT, loggedUser).openChannel();

            ChatClient client = new ChatClient("127.0.0.1", 9898, loggedUser);
            client.execute();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void callOff() {
        System.out.println("callOff");
    }

    public ImageView getLoggedUserImageView() {
        return this.loggedUser;
    }
}
