package com.roava.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

class PlayThread extends Thread {
    int dataPacketSize = 4096;
    AudioInputStream inputStream;
    SourceDataLine sourceLine;
    byte tempBuffer[] = new byte[dataPacketSize];

    public PlayThread(SourceDataLine sourceLine, AudioInputStream audioInputStream) {
        this.sourceLine = sourceLine;
        this.inputStream = audioInputStream;
    }

    public void run() {
        if (sourceLine == null || inputStream == null) {
            System.out.println("source or audio stream null");
        }
        try {
            int cnt;
            while ((cnt = inputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                if (cnt > 0) {
                    System.out.println(">>>>>> server to speaker");
                    sourceLine.write(tempBuffer, 0, cnt);
                }
                Thread.yield();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
            //System.exit(0);
        }
    }
}
