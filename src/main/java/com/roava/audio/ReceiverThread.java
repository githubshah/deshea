package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class ReceiverThread extends Thread {

    private DatagramSocket clientReceiverSocket;
    private boolean stopped = false;
    private SourceDataLine sourceDataLine;
    private AudioInputStream audioInputStream;
    private int serverPort;
    private InetAddress serverIPAddress;

    public ReceiverThread(InetAddress serverIPAddress, int serverPort) throws SocketException, LineUnavailableException {
        this.serverIPAddress = serverIPAddress;
        this.serverPort = serverPort;
        // Create client DatagramSocket
        this.clientReceiverSocket = new DatagramSocket();
        this.clientReceiverSocket.connect(serverIPAddress, serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        System.out.println("Sender Thread Connected to the server "
            + this.serverIPAddress.getHostAddress() + ":" + this.serverPort);
        try {
            byte[] sendData = new byte[10];
            sendData = "RECEIVER".getBytes( StandardCharsets.UTF_8 );
            clientReceiverSocket.send(new DatagramPacket(sendData,sendData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.openSpeaker();
        System.out.println("Sender Thread Opened Speaker");
    }

    @Override
    public void run() {
        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[UtilAudio.getBufferSize];
        AudioFormat audioFormat = UtilAudio.getAudioFormat();
        while (true) {
            if (stopped) { // todo
                return;
            }
            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                // Receive a packet from the server (blocks until the packets are received)
                //clientReceiverSocket.setSoTimeout(10000); // timeout 10 sec
                System.out.println("waiting to receive data from server...");
                clientReceiverSocket.receive(receivePacket);
                try {
                    InputStream byteInputStream = new ByteArrayInputStream(receivePacket.getData());
                    audioInputStream = new AudioInputStream(byteInputStream, audioFormat, receivePacket.getData().length / audioFormat.getFrameSize());
                    PlayThread playThread = new PlayThread();
                    playThread.start();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
            Thread.yield();
        }
    }

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[4096];

        public void run() {
            try {
                int cnt;
                //Keep looping until the input read method
                // returns -1 for empty stream.
                while ((cnt = audioInputStream.read(
                    tempBuffer, 0,
                    tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        //Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }//end if
                }//end while
                //Block and wait for internal buffer of the
                // data line to empty.
                sourceDataLine.drain();
                sourceDataLine.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }//end catch
        }//end run
    }//end inner class PlayThread

    private void openSpeaker() throws LineUnavailableException {
        AudioFormat adFormat = UtilAudio.getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceDataLine.open(adFormat);
        sourceDataLine.start();
    }
}