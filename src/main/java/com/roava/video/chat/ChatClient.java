package com.roava.video.chat;

import javafx.scene.image.ImageView;

import java.net.*;
import java.io.*;
 
/**
 * This is the chat client program.
 * Type 'bye' to terminte the program.
 *
 * @author www.codejava.net
 */
public class ChatClient {
    private String hostname;
    private int port;
    private String userName;

    public ImageView getLoggedUser() {
        return loggedUser;
    }

    ImageView loggedUser;
 
    public ChatClient(String hostname, int port, ImageView loggedUser) {
        this.hostname = hostname;
        this.port = port;
    }
 
    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
 
            System.out.println("Connected to the chat server");
 
//            new ReadThread(socket, this).start();
//            new WriteThread(socket, this).start();
 
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
 
    }
 
    void setUserName(String userName) {
        this.userName = userName;
    }
 
    String getUserName() {
        return this.userName;
    }
 
 
    public static void main(String[] args) {
//        ChatClient client = new ChatClient("127.0.0.1",9898);
//        client.execute();
    }
}