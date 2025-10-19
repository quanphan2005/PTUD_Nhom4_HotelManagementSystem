package vn.iuh.util;

import io.github.cdimascio.dotenv.Dotenv;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayer {
    private static void playNotificationSound(String filePath) {
        try (InputStream inputStream = AudioPlayer.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                return;
            }

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                // Đợi phát xong
                Thread.sleep(clip.getMicrosecondLength() / 800);
                clip.close();
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println( e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println( e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void playDefaultNotification() {
        playNotificationSound("/sound/notification.wav");
    }
}
