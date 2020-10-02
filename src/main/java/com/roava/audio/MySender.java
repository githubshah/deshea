package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MySender {

    AudioFormat audioFormat;

    TargetDataLine targetDataLine;

    private AudioFormat getAudioFormat() {
//        return new AudioFormat(
//            AudioFormat.Encoding.PCM_SIGNED,
//            44100.0F,
//            16,
//            2,
//            2 * 2,
//            44100.0F,
//            false);

        //return new AudioFormat(44100.0f, 16, 2, true, true);
        //return new AudioFormat(44100, 16, 4, true, true);
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            44100.0F,16,2,2*2, 44100.0f,true);

    }//end getAudioFormat


    public void m1ain() throws LineUnavailableException {
        Mixer mixer = AudioSystem.getMixer(null); // default mixer
        mixer.open();

        System.out.printf("Supported SourceDataLines of default mixer (%s):\n\n", mixer.getMixerInfo().getName());
        for (Line.Info info : mixer.getSourceLineInfo()) {
            if (SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
                SourceDataLine.Info info2 = (SourceDataLine.Info) info;
                System.out.println(info2);
                System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
                System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
                javax.sound.sampled.AudioFormat[] formats = info2.getFormats();
                System.out.println("  Supported Audio formats: ");
                for (javax.sound.sampled.AudioFormat format : formats) {
                    System.out.println("    " + format);
                    // System.out.printf(" encoding: %s\n", format.getEncoding());
                    // System.out.printf(" channels: %d\n", format.getChannels());
                    // System.out.printf(format.getFrameRate()==-1?"":" frame rate [1/s]: %s\n",
                    // format.getFrameRate());
                    // System.out.printf(" frame size [bytes]: %d\n", format.getFrameSize());
                    // System.out.printf(format.getSampleRate()==-1?"":" sample rate [1/s]: %s\n",
                    // format.getSampleRate());
                    // System.out.printf(" sample size [bit]: %d\n", format.getSampleSizeInBits());
                    // System.out.printf(" big endian: %b\n", format.isBigEndian());
                    //
                    // Map<String,Object> prop = format.properties();
                    // if(!prop.isEmpty()) {
                    // System.out.println(" Properties: ");
                    // for(Map.Entry<String, Object> entry : prop.entrySet()) {
                    // System.out.printf(" %s: \t%s\n", entry.getKey(), entry.getValue());
                    // }
                    // }
                }
                System.out.println();
            } else {
                System.out.println(info.toString());
            }
            System.out.println();
        }

        mixer.close();

    }

    public void captureAudio() {
        try {
            //Get and display a list of
            // available mixers.
            m1ain();
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            System.out.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                System.out.println(mixerInfo[cnt].getName());
            }//end for loop

            //Get everything set up for capture
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo =
                new DataLine.Info(
                    TargetDataLine.class,
                    audioFormat);

            //Select one of the available
            // mixers.
            Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);

            //Get a TargetDataLine on the selected
            // mixer.
            targetDataLine = (TargetDataLine)
                mixer.getLine(dataLineInfo);
            //Prepare the line for use.
            targetDataLine.open(audioFormat);
            targetDataLine.start();



            //Create a thread to capture the microphone
            // data and start it running.  It will run
            // until the Stop button is clicked.
            Thread captureThread = new CaptureThread();
            captureThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }//end catch
    }//end captureAudio method

    class CaptureThread extends Thread {
        //An arbitrary-size temporary holding buffer
        byte tempBuffer[] = new byte[40000];

        public void run() {
            DatagramSocket clientSenderSocket = null;
            try {
                clientSenderSocket = new DatagramSocket();
                clientSenderSocket.connect(InetAddress.getByName("localhost"), 9786);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }


            try {//Loop until stopCapture is set by
                // another thread that services the Stop
                // button.
                while (true) {
                    //Read data from the internal buffer of
                    // the data line.

                    int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                    if (cnt > 0) {
                        System.out.println(tempBuffer.length == cnt);
                        //Save data in output stream object.

                        //send data to server
                        clientSenderSocket.send(new DatagramPacket(tempBuffer, tempBuffer.length));

                        //byteArrayOutputStream.write(tempBuffer, 0, cnt);
                    }//end if
                }//end while
                //byteArrayOutputStream.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }//end catch
        }//end run
    }//end inner class CaptureThread
}
