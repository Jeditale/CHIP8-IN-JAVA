import cpu.Chip8;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;

public class Main implements KeyListener {

    private static final int SCALE_FACTOR = 15;
    private static final int CHIP8_WIDTH = 64;
    private static final int CHIP8_HEIGHT = 32;
    private static final int SCREEN_WIDTH = CHIP8_WIDTH * SCALE_FACTOR;
    private static final int SCREEN_HEIGHT = CHIP8_HEIGHT * SCALE_FACTOR;
    private final ScreenPanel panel;
    private SourceDataLine audioLine;
    private byte[] beepBuffer;

    private final Chip8 chip8;

    private final Map<Integer, Integer> keyMap = new HashMap<>();

    public Main() {
        chip8 = new Chip8();
        chip8.reset();
        chip8.loadROM("ROMS/Pong (alt).ch8");

        panel = new ScreenPanel();

        JFrame frame = new JFrame("CHIP-8 Emulator");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);


        frame.addKeyListener(this);

        frame.setVisible(true);
        try {
            initAudio();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable. Sound will be disabled.");
            e.printStackTrace();
        }

        runEmulator();
    }
    private void initAudio() throws LineUnavailableException {

        AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, true);

        audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();

        beepBuffer = new byte[2048];

        double angleIncrement = 2.0 * Math.PI * 440.0 / 44100.0;
        double currentAngle = 0.0;

        for (int i = 0; i < beepBuffer.length; i += 4) {
            short sample = (short)(Math.sin(currentAngle) * 32767.0);

            // Left Channel
            beepBuffer[i]   = (byte)(sample >> 8);
            beepBuffer[i+1] = (byte)(sample & 0xFF);

            // Right Channel (same sample)
            beepBuffer[i+2] = (byte)(sample >> 8);
            beepBuffer[i+3] = (byte)(sample & 0xFF);

            currentAngle += angleIncrement;
        }
    }

    public void runEmulator() {

        // If it's too slow, raise it.
        final int CYCLES_PER_FRAME = 15;

        // --- The 60Hz (60fps) Timer ---
        long lastTime = System.nanoTime();
        double nsPerFrame = 1000000000.0 / 60.0;
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerFrame;
            lastTime = now;

            if (delta >= 1) {

                for (int i = 0; i < CYCLES_PER_FRAME; i++) {
                    chip8.cycle();
                }

                chip8.updateTimers();
                if (audioLine != null) {
                    if (chip8.getSoundTimer() > 0) {

                        audioLine.write(beepBuffer, 0, beepBuffer.length);
                    } else {
                        audioLine.flush();
                    }
                }

                panel.updateScreen(chip8.getScreen());
                panel.repaint();

                delta--;
            }

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Map 1, 2, 3, 4, Q, W, E, R, A, S, D, F, Z, X, C, V
        // to 1, 2, 3, C, 4, 5, 6, D, 7, 8, 9, E, A, 0, B, F
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_1) chip8.setKey(0x1, true);
        if (keyCode == KeyEvent.VK_2) chip8.setKey(0x2, true);
        if (keyCode == KeyEvent.VK_3) chip8.setKey(0x3, true);
        if (keyCode == KeyEvent.VK_4) chip8.setKey(0xC, true);

        if (keyCode == KeyEvent.VK_Q) chip8.setKey(0x4, true);
        if (keyCode == KeyEvent.VK_W) chip8.setKey(0x5, true);
        if (keyCode == KeyEvent.VK_E) chip8.setKey(0x6, true);
        if (keyCode == KeyEvent.VK_R) chip8.setKey(0xD, true);

        if (keyCode == KeyEvent.VK_A) chip8.setKey(0x7, true);
        if (keyCode == KeyEvent.VK_S) chip8.setKey(0x8, true);
        if (keyCode == KeyEvent.VK_D) chip8.setKey(0x9, true);
        if (keyCode == KeyEvent.VK_F) chip8.setKey(0xE, true);

        if (keyCode == KeyEvent.VK_Z) chip8.setKey(0xA, true);
        if (keyCode == KeyEvent.VK_X) chip8.setKey(0x0, true);
        if (keyCode == KeyEvent.VK_C) chip8.setKey(0xB, true);
        if (keyCode == KeyEvent.VK_V) chip8.setKey(0xF, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_1) chip8.setKey(0x1, false);
        if (keyCode == KeyEvent.VK_2) chip8.setKey(0x2, false);
        if (keyCode == KeyEvent.VK_3) chip8.setKey(0x3, false);
        if (keyCode == KeyEvent.VK_4) chip8.setKey(0xC, false);

        if (keyCode == KeyEvent.VK_Q) chip8.setKey(0x4, false);
        if (keyCode == KeyEvent.VK_W) chip8.setKey(0x5, false);
        if (keyCode == KeyEvent.VK_E) chip8.setKey(0x6, false);
        if (keyCode == KeyEvent.VK_R) chip8.setKey(0xD, false);

        if (keyCode == KeyEvent.VK_A) chip8.setKey(0x7, false);
        if (keyCode == KeyEvent.VK_S) chip8.setKey(0x8, false);
        if (keyCode == KeyEvent.VK_D) chip8.setKey(0x9, false);
        if (keyCode == KeyEvent.VK_F) chip8.setKey(0xE, false);

        if (keyCode == KeyEvent.VK_Z) chip8.setKey(0xA, false);
        if (keyCode == KeyEvent.VK_X) chip8.setKey(0x0, false);
        if (keyCode == KeyEvent.VK_C) chip8.setKey(0xB, false);
        if (keyCode == KeyEvent.VK_V) chip8.setKey(0xF, false);
    }

    public static void main(String[] args) {
        new Main();
    }

    private static class ScreenPanel extends JPanel {
        private final BufferedImage buffer;
        private final int[] raster;

        public ScreenPanel() {
            this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            buffer = new BufferedImage(CHIP8_WIDTH, CHIP8_HEIGHT, BufferedImage.TYPE_INT_RGB);
            raster = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        }

        public void updateScreen(int[][] chip8Screen) {
            int i = 0;
            for (int y = 0; y < CHIP8_HEIGHT; y++) {
                for (int x = 0; x < CHIP8_WIDTH; x++) {
                    if (chip8Screen[y][x] == 1) {
                        raster[i] = 0x1DFF00; // GREEN GREEN WHAT'S YOUR PROBLEM
                    } else {
                        raster[i] = 0x000000;
                    }
                    i++;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(buffer, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }
    }

}