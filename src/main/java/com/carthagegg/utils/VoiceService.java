package com.carthagegg.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VoiceService {

    private static Process ttsProcess;

    /**
     * Speaks the given text using Windows PowerShell SpeechSynthesizer.
     * Stops any currently playing speech before starting.
     */
    public static void speak(String text, Runnable onComplete) {
        stopSpeaking();
        if (text == null || text.trim().isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        new Thread(() -> {
            File tempFile = null;
            try {
                // Write text to a temporary file to avoid command-line length limits and escaping issues
                tempFile = File.createTempFile("tts_", ".txt");
                Files.writeString(tempFile.toPath(), text, StandardCharsets.UTF_8);

                String script = 
                    "Add-Type -AssemblyName System.speech; " +
                    "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$text = Get-Content -Path '" + tempFile.getAbsolutePath() + "' -Encoding UTF8 -Raw; " +
                    "$synth.Speak($text);";

                // Encode the powershell command in Base64 to avoid ANY quoting issues in the shell
                String base64Cmd = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));

                ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-NoProfile", "-EncodedCommand", base64Cmd);
                ttsProcess = pb.start();
                ttsProcess.waitFor();
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Cleanup temp file after speaking
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                if (onComplete != null) {
                    onComplete.run();
                }
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
