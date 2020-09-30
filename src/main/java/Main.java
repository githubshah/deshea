
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    final int serverPort = 9786;
    DatagramSocket udpServerSocket;
    ByteArrayOutputStream byteArrayOutputStream;

    public void runVOIP() {
        try {
            udpServerSocket = new DatagramSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[10000];

            new Thread(() -> {
                try {
                    Thread.sleep(30000);
                    System.out.println("Byte stream closed");
                    byteArrayOutputStream.close();
                    //playAudio();
                    toFile();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }).start();

            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(tempBuffer, tempBuffer.length);
                System.out.println("Server waiting to retrieve: " + serverPort);
                udpServerSocket.receive(receivePacket);
                try {
                    System.out.println("write some thing");
                    executor.execute(() -> {
                        byteArrayOutputStream.write(tempBuffer, 0, tempBuffer.length);
                    });
                } catch (Exception e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toFile() throws IOException {
        File dstFile = new File("/home/ubuntu/shah/dst.wav");
        FileOutputStream out = new FileOutputStream(dstFile);
        byte audioData[] = byteArrayOutputStream.toByteArray();
        InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioInputStream leftoutputAIS = new AudioInputStream(byteArrayInputStream, getAudioFormat(), audioData.length / getAudioFormat().getFrameSize());
        AudioSystem.write(leftoutputAIS, AudioFileFormat.Type.WAVE, dstFile);

    }

    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;

    //This method plays back the audio data that
    // has been saved in the ByteArrayOutputStream
    private void playAudio() {
        try {
            //Get everything set up for playback.
            //Get the previously-saved data into a byte
            // array object.
            byte audioData[] = byteArrayOutputStream.
                toByteArray();
            //Get an input stream on the byte array
            // containing the data
            InputStream byteArrayInputStream =
                new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(
                byteArrayInputStream,
                audioFormat,
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
            Thread playThread = new PlayThread();
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }//end catch
    }//end playAudio

    class PlayThread extends Thread {
        byte tempBuffer[] = new byte[10000];

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

    //This method creates and returns an
    // AudioFormat object for a given set of format
    // parameters.  If these parameters don't work
    // well for you, try some of the other
    // allowable parameter values, which are shown
    // in comments following the declartions.
    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
            sampleRate,
            sampleSizeInBits,
            channels,
            signed,
            bigEndian);
    }//end getAudioFormat

    public static void main(String args[]) throws Exception {
        new Main().runVOIP();
    }
}