package com.carthagegg.utils;

import javax.sound.sampled.*;
import java.io.File;

public class MicrophoneService {

    private TargetDataLine targetLine;
    private File outputFile;

    public void startRecording(File file) throws Exception {
        outputFile = file;
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false); // 16kHz, 16-bit, Mono, Signed, Little-Endian
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Microphone is not supported.");
        }

        targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(format);
        targetLine.start();

        new Thread(() -> {
            try {
                AudioInputStream audioStream = new AudioInputStream(targetLine);
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopRecording() {
        if (targetLine != null) {
            targetLine.stop();
            targetLine.close();
            targetLine = null;
        }
    }
}
