package com.roava;

import com.google.gson.Gson;
import com.roava.core.WebCam;
import com.roava.video.patient.ReadThread;
import javafx.scene.image.ImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketBridge {
    final String type = "patient";
    final String email = "patient@gmail.com";
    private Socket eventSocket;

    public SocketBridge() throws IOException {
        eventSocket = new Socket("3.16.216.89", 9897);
        new Thread(new ListenerThread(eventSocket)).start();
    }

    public void connectToServer() throws IOException {
        MessagePacket connect = new MessagePacket(email, Constants.CONNECT_TO_RECEPTIONIST, type);
        DataOutputStream dout = new DataOutputStream(eventSocket.getOutputStream());
        dout.writeUTF(new Gson().toJson(connect));
    }

    ImageView imageView;

    public void renderIn(ImageView imageView) {
        this.imageView = imageView;
    }

    class ListenerThread implements Runnable {
        Socket eventSocket;

        public ListenerThread(Socket eventSocket) {
            this.eventSocket = eventSocket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    DataInputStream din = new DataInputStream(eventSocket.getInputStream());
                    String msg = din.readUTF();
                    System.out.println(msg);
                    MessagePacket messagePacket = new Gson().fromJson(msg, MessagePacket.class);
                    switch (messagePacket.getEvent()) {
                        case Constants.RECEPTIONIST_NOT_AVAILABLE:
                            System.out.println("RECEPTIONIST_NOT_AVAILABLE");
                            break;
                        case Constants.RECEPTIONIST_AVAILABLE:
                            System.out.println("RECEPTIONIST_AVAILABLE");
                            this.startVideoCalling();
                            break;
                        default:
                    }
                } catch (IOException ex) {
                    System.out.println("Error in chat.UserThread: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        private void startVideoCalling() throws IOException {
            Socket socket = new Socket("3.16.216.89", 9898);
            System.out.println("Connected to the chat server");
            WebCam webCam = new WebCam();
            webCam.populateInSocket(socket);
            webCam.start();
            new ReadThread(socket, imageView).start();
        }
    }
}
