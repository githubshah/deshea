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
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public TargetDataLine openMicLine() throws LineUnavailableException {
        try {
            micLine.open(adFormat);
            this.micLine.start();
            return this.micLine;
        } catch (LineUnavailableException e) {
            throw new LineUnavailableException();
        }
    }
}