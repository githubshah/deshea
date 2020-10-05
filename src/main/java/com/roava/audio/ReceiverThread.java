package com.roava.audio;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
        try {
            byte[] sendData = new byte[10];
            sendData = "RECEIVER".getBytes(StandardCharsets.UTF_8);
            clientReceiverSocket.send(new DatagramPacket(sendData, sendData.length));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.openSpeaker();
        System.out.println("Sender Thread Opened Speaker");
    }

    ByteArrayOutputStream byteArrayOutputStream;

    private void toFile(ByteArrayOutputStream byteArrayOutputStream, String fileNem) throws IOException {
        //File dstFile = new File("/home/ubuntu/shah/dst.mp3");
        System.out.println("Going to create file");
        File dstFile = new File(fileNem);
        byte audioData[] = byteArrayOutputStream.toByteArray();
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioInputStream leftoutputAIS = new AudioInputStream(byteArrayInputStream, UtilAudio.getAudioFormat(), audioData.length / UtilAudio.getAudioFormat().getFrameSize());
        AudioSystem.write(leftoutputAIS, AudioFileFormat.Type.WAVE, dstFile);
    }


    private void playAudio() {
        try {
            //Get everything set up for playback.
            //Get the previously-saved data into a byte
            // array object.
            byte audioData[] = byteArrayOutputStream.toByteArray();
            //Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream =
                new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = UtilAudio.getAudioFormat();
            audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
                audioData.length / audioFormat.
                    getFrameSize());
            DataLine.Info dataLineInfo =
                new DataLine.Info(
                    SourceDataLine.class,
                    audioFormat);
            sourceDataLine = (SourceDataLine)
                AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            //Create a thread to play back the data and
            // start it  running.  It will run until
            // all the data has been played back.
            System.out.println("Going to play sound...");
            Thread playThread = new PlayThread();
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }//end catch
    }//end playAudio

    private void StartRecording() {
        new Thread(() -> {
            try {
                Thread.sleep(20000);
                System.out.println(">>>>>>>>>>>>>>>>>>Byte stream closed");
                System.out.println(">>>>>>>>>>>>>>>>>>Byte stream closed");
                System.out.println(">>>>>>>>>>>>>>>>>>Byte stream closed");
                System.out.println(">>>>>>>>>>>>>>>>>>Byte stream closed");
                byteArrayOutputStream.close();
                //toFile(byteArrayOutputStream, "fn.mp3");
                playAudio();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Override
    public void run() {
        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[UtilAudio.getBufferSize];
        AudioFormat audioFormat = UtilAudio.getAudioFormat();
        byteArrayOutputStream = new ByteArrayOutputStream();
        //StartRecording();
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
                try{
                    clientReceiverSocket.setSoTimeout(5000);
                    clientReceiverSocket.receive(receivePacket);
                }catch (Exception e){
                    System.out.println("drop waiting...");
                    continue;
                }
                //clientReceiverSocket.receive(receivePacket);
                try {
                    //byteArrayOutputStream.write(receiveData, 0, receiveData.length);

                    InputStream byteInputStream = new ByteArrayInputStream(receivePacket.getData());
                    audioInputStream = new AudioInputStream(byteInputStream, audioFormat, receivePacket.getData().length / audioFormat.getFrameSize());
                    executor.execute(() -> {
                        byte tempBuffer[] = new byte[UtilAudio.getBufferSize];

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
                            //sourceDataLine.drain();
                            //sourceDataLine.close();
                        } catch (Exception e) {
                            System.out.println(e);
                            System.exit(0);
                        }//end catch
                    });
//                    PlayThread playThread = new PlayThread();
//                    playThread.start();
                } catch (Exception e) {
                    e.getStackTrace();
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Thread.yield();
        }
    }

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[UtilAudio.getBufferSize];

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
                //sourceDataLine.drain();
                //sourceDataLine.close();
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