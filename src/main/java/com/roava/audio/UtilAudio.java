package com.roava.audio;

import javax.sound.sampled.AudioFormat;

public class UtilAudio {
    public static AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            44100.0F, 16, 2, 2 * 2, 44100.0f, true);
    }

    final static int getBufferSize = 60000;
}
