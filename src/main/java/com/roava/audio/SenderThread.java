package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class SenderThread extends Thread {

    private InetAddress serverIPAddress;
    private DatagramSocket clientSenderSocket;
    private int serverPort;
    private TargetDataLine targetDataLine;

    public SenderThread(InetAddress serverIPAddress, int serverPort)
        throws SocketException, LineUnavailableException {
        //
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        System.out.println("Available mixers:");
        for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
            System.out.println(mixerInfo[cnt].getName());
        }//end for loop

        AudioFormat audioFormat = UtilAudio.getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);
        targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        //
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;

        // Create client DatagramSocket
        this.clientSenderSocket = new DatagramSocket();
        this.clientSenderSocket.connect(serverIPAddress, serverPort);

        // connect to server
        this.requestToConnect();
    }

    private void requestToConnect() {
        try {
            byte[] sendData = new byte[10];
            sendData = "SENDER".getBytes(StandardCharsets.UTF_8);
            clientSenderSocket.send(new DatagramPacket(sendData, sendData.length));
            System.out.println("Sender Thread Connected to the server "
                + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //An arbitrary-size temporary holding buffer
        byte tempBuffer[] = new byte[UtilAudio.getBufferSize];

        try {//Loop until stopCapture is set by
            // another thread that services the Stop
            // button.
            while (true) {
                //Read data from the internal buffer of
                // the data line.
                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                if (cnt > 0) {
                    System.out.println(tempBuffer.length == cnt);
                    //send data to server
                    clientSenderSocket.send(new DatagramPacket(tempBuffer, tempBuffer.length));
                }//end if
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}