package com.roava.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Mic {

    private AudioFormat adFormat;
    private TargetDataLine micLine;

    public Mic() {
        try {
            adFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);

            if (!AudioSystem.isLineSupported(dataLineInfo)) {
                System.out.println("not supported");
                return;
            }

            micLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        } catch (Exception e) {
            StackTraceElement stackEle[] = e.getStackTrace();
            for (StackTraceElement val : stackEle) {
                System.out.println(val);
            }
            System.exit(0);
        }
    }

    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 44100.0f;
        int channels = 2;
        int frameSize = 4;
        int sampleSize = 16;
        boolean bigEndian = true;

        return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
            * channels, rate, bigEndian);
    }

    public TargetDataLine openMicLine() throws LineUnavailableException {
        try {
            micLine.open(adFormat, micLine.getBufferSize());
            this.micLine.start();
            return this.micLine;
        } catch (LineUnavailableException e) {
            throw new LineUnavailableException();
        }
    }
}