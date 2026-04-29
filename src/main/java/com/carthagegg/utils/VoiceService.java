package com.carthagegg.utils;

import java.io.File;

public class VoiceService {

    private static Process ttsProcess;

    /**
     * Speaks the given text using Windows PowerShell SpeechSynthesizer.
     * Stops any currently playing speech before starting.
     */
    public static void speak(String text) {
        stopSpeaking();
        if (text == null || text.trim().isEmpty()) return;

        new Thread(() -> {
            try {
                // Escape characters for PowerShell string
                String safeText = text.replace("'", "''").replace("\"", " ");
                // Remove newlines which might break the powershell command
                safeText = safeText.replace("\n", " ").replace("\r", " ");

                String cmd = "Add-Type -AssemblyName System.speech; " +
                             "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                             "$synth.Speak('" + safeText + "')";

                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", cmd);
                ttsProcess = pb.start();
                ttsProcess.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stops the current speech process if it is running.
     */
    public static void stopSpeaking() {
        if (ttsProcess != null && ttsProcess.isAlive()) {
            ttsProcess.destroyForcibly();
            ttsProcess = null;
        }
    }
}
