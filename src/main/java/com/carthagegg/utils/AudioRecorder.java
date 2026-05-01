package com.carthagegg.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private TargetDataLine line;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private File wavFile;
    private Thread recordingThread;

    public File startRecording() throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new Exception("Microphone not supported.");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        wavFile = File.createTempFile("stt_record_", ".wav");
        wavFile.deleteOnExit();

        recordingThread = new Thread(() -> {
            try {
                AudioInputStream ais = new AudioInputStream(line);
                AudioSystem.write(ais, fileType, wavFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        recordingThread.start();

        return wavFile;
    }

    public File stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
        }
        return wavFile;
    }
}
