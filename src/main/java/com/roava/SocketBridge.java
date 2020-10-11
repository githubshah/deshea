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
    private Socket eventSocket;

    public SocketBridge() throws IOException {
        eventSocket = new Socket("127.0.0.1", 9897);
        new Thread(new ListenerThread(eventSocket)).start();
    }

    public void connectToServer() throws IOException {
        Client connect = new Client("sahid@gmail.com", "connect");
        DataOutputStream dout = new DataOutputStream(eventSocket.getOutputStream());
        dout.writeUTF(new Gson().toJson(connect));
    }

    public void disconnectFromServer() throws IOException {
        Client connect = new Client("sahid@gmail.com", "disconnect");
        DataOutputStream dout = new DataOutputStream(eventSocket.getOutputStream());
        dout.writeUTF(new Gson().toJson(connect));
    }

    public void getListOfPatient() throws IOException {
        Client connect = new Client("sahid@gmail.com", "patientlist");
        DataOutputStream dout = new DataOutputStream(eventSocket.getOutputStream());
        dout.writeUTF(new Gson().toJson(connect));
    }

    public void getListOfReceptionist() throws IOException {
        Client connect = new Client("sahid@gmail.com", "receptionistlist");
        DataOutputStream dout = new DataOutputStream(eventSocket.getOutputStream());
        dout.writeUTF(new Gson().toJson(connect));
    }

    public void getListOfConnection() throws IOException {
        Client connect = new Client("sahid@gmail.com", "connectionlist");
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
                    Client client = new Gson().fromJson(msg, Client.class);
                    switch (client.getEvent()) {
                        case "receptionistavailable":
                            System.out.println("avail recp");
                            this.startVideoCalling();
                            break;
                        case "noreceptionist":
                            System.out.println("no recep");
                            break;
                        case "connactto":
                            break;
                        case "patientlist":
                            break;
                        case "receptionistlist":
                            break;
                        case "connectionlist":
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
            Socket socket = new Socket("127.0.0.1", 9898);
            System.out.println("Connected to the chat server");
            WebCam webCam = new WebCam();
            webCam.populateInSocket(socket);
            webCam.start();
            new ReadThread(socket, imageView).start();
        }
    }
}
