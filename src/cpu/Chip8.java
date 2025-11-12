package cpu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Chip8 {
    private int[] memory = new int[4096];
    private int[] vRegister = new int[16];
    private int iRegister;
    private int pc;
    private int[][] screen = new int[32][64];
    private int[] stack = new int[16];
    private int sp;
    private int delayTimer;
    private int soundTimer;
    private boolean[] keys = new boolean[16];

    public int[][] getScreen() {
        return this.screen;
    }

    public void reset() {
        Arrays.fill(memory, 0);
        Arrays.fill(vRegister, 0);
        iRegister = 0;
        pc = 512;
        sp = 0;
        delayTimer = 0;
        soundTimer = 0;
        Arrays.fill(keys, false);
    }

    public void cycle() {
        System.out.println("RUN CYCLE:" + pc);
        int first = memory[pc];
        int second = memory[pc + 1];
        int opcode = (first << 8) | second;

        int command = (opcode & 0xF000) >> 12;


        switch (command) {
            case 0x1: {
                pc = (opcode & 0x0FFF);
                break;
            }
            case 0x2: {
                stack[sp] = pc;
                sp++;
                pc = (opcode & 0x0FFF);
                break;
            }
            case 0x3: {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                if (vRegister[x] == nn)
                    pc += 2;
                pc += 2;
                break;
            }
            case 0x4: {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                if (vRegister[x] != nn)
                    pc += 2;
                pc += 2;
                break;
            }
            case 0x5: {
                int x = (opcode & 0x0F00) >> 8;
                int y = (opcode & 0x00F0) >> 4;
                if ((opcode & 0x000F) == 0x0)
                    if (vRegister[x] == vRegister[y])
                        pc += 2;
                pc += 2;
                break;
            }
            case 0x6: {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                vRegister[x] = nn;
                pc += 2;
                break;
            }
            case 0x7: {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                vRegister[x] += nn;
                pc += 2;
                break;
            }
            case 0x8: {
                int x = (opcode & 0x0F00) >> 8;
                int y = (opcode & 0x00F0) >> 4;
                int n = (opcode & 0x000F);
                switch (n) {
                    case 0x0: {
                        vRegister[x] = vRegister[y];
                        break;
                    }
                    case 0x1: {
                        vRegister[x] |= vRegister[y];
                        break;
                    }
                    case 0x2: {
                        vRegister[x] &= vRegister[y];
                        break;
                    }
                    case 0x3: {
                        vRegister[x] ^= vRegister[y];
                        break;
                    }
                    case 0x4: {
                        int sum = vRegister[x] + vRegister[y];
                        if (sum > 255) {
                            vRegister[15] = 1;
                        } else {
                            vRegister[15] = 0;
                        }
                        vRegister[x] = sum & 0xFF;
                        break;
                    }
                    case 0x5: {
                        if (vRegister[x] >= vRegister[y]) {
                            vRegister[15] = 1;
                        } else {
                            vRegister[15] = 0;
                        }
                        vRegister[x] = (vRegister[x] - vRegister[y]) & 0xFF;
                        break;
                    }
                    case 0x6: {
                        vRegister[15] = vRegister[x] & 0x1;
                        vRegister[x] = vRegister[x] >> 1;
                        break;
                    }
                    case 0x7: {
                        if (vRegister[y] >= vRegister[x]) {
                            vRegister[15] = 1;
                        } else {
                            vRegister[15] = 0;
                        }

                        vRegister[x] = (vRegister[y] - vRegister[x]) & 0xFF;
                        break;
                    }
                    case 0xE: {
                        vRegister[15] = (vRegister[x] & 0x80) >> 7;
                        vRegister[x] = (vRegister[x] << 1) & 0xFF;
                        break;
                    }
                }
                pc += 2;
                break;
            }
            case 0xA: {
                int nnn = (opcode & 0x0FFF);
                iRegister = nnn;
                pc += 2;
                break;
            }
            case 0x9: {
                int x = (opcode & 0x0F00) >> 8;
                int y = (opcode & 0x00F0) >> 4;
                if ((opcode & 0x000F) == 0x0)
                    if (vRegister[x] != vRegister[y])
                        pc += 2;
                pc += 2;
                break;
            }
            case 0x0: {
                switch (opcode & 0x00FF) {
                    case 0xE0: {
                        for (int y = 0; y < 32; y++) {
                            for (int x = 0; x < 64; x++) {
                                screen[y][x] = 0;
                            }
                        }
                        pc += 2;
                        break;
                    }
                    case 0xEE: {
                        sp--;
                        pc = stack[sp];
                        pc += 2;
                        break;
                    }
                    default: {
                        pc += 2;
                        break;
                    }
                }
                break;
            }
            case 0xC: {
                int x = (opcode & 0x0F00) >> 8;
                int kk = (opcode & 0x00FF);

                int randomByte = (int) (Math.random() * 256);

                vRegister[x] = randomByte & kk;
                pc += 2;
                break;
            }
            case 0xD: {
                int x = vRegister[(opcode & 0x0F00) >> 8];
                int y = vRegister[(opcode & 0x00F0) >> 4];
                int h = (opcode & 0x000F);
                vRegister[15] = 0;

                for (int i = 0; i < h; i++) {
                    int spriteByte = memory[iRegister + i];
                    for (int j = 0; j < 8; j++) {
                        if ((spriteByte & (128 >> j)) != 0) {
                            int screenX = (x + j) % 64;
                            int screenY = (y + i) % 32;
                            if (screen[screenY][screenX] == 1) {
                                vRegister[15] = 1;
                            }
                            screen[screenY][screenX] ^= 1;

                        }
                    }
                }
                pc += 2;
                break;
            }
            case 0xE: {
                int x = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    case 0x9E: {
                        if (keys[vRegister[x]]) {
                            pc += 2;
                        }
                        break;
                    }
                    case 0xA1: {
                        if (!keys[vRegister[x]]) {
                            pc += 2;
                        }
                        break;
                    }
                }
                pc += 2;
                break;
            }
            case 0xF: {
                int x = (opcode & 0x0F00) >> 8;
                switch(opcode & 0x00FF) {
                    case 0x07: {
                        vRegister[x] = delayTimer;
                        pc += 2;
                        break;
                    }
                    case 0x0A: {
                        boolean isKeyPressed = false;
                        for (int i = 0; i < 16; i++) {
                            if (keys[i]) {
                                vRegister[x] = i;
                                isKeyPressed = true;
                                break;
                            }
                        }
                        if (isKeyPressed) {
                            pc += 2;
                        }
                        break;
                    }
                    case 0x15: { // Fx15 - LD DT, Vx
                        delayTimer = vRegister[x];
                        pc += 2;
                        break;
                    }
                    case 0x18: { // Fx18 - LD ST, Vx
                        soundTimer = vRegister[x];
                        pc += 2;
                        break;
                    }
                    case 0x1E: { // Fx1E - ADD I, Vx
                        iRegister += vRegister[x];
                        pc += 2;
                        break;
                    }
                    case 0x29: {
                        iRegister = (vRegister[x] & 0x0F) * 5;
                        pc += 2;
                        break;
                    }
                    case 0x33: {
                        int val = vRegister[x];
                        memory[iRegister]     = (val / 100) % 10;
                        memory[iRegister + 1] = (val / 10)  % 10;
                        memory[iRegister + 2] = (val / 1)   % 10;
                        pc += 2;
                        break;
                    }
                    case 0x55: { // Fx55 - LD [I], Vx
                        for (int i = 0; i <= x; i++) {
                            memory[iRegister + i] = vRegister[i];
                        }
                        pc += 2;
                        break;
                    }
                    case 0x65: { // Fx65 - LD Vx, [I]
                        for (int i = 0; i <= x; i++) {
                            vRegister[i] = memory[iRegister + i];
                        }
                        pc += 2;
                        break;
                    }
                }
                break;
            }
            default: {
                pc += 2;
                break;
            }

        }


    }

    public void dumpState() {
        System.out.println("--- CPU State ---");
        System.out.println("PC: 0x" + Integer.toHexString(pc));
        for (int i = 0; i < 16; i++) {
            System.out.printf("V[%X]: 0x%02X  ", i, vRegister[i]);
            if (i % 4 == 3) System.out.println(); // Newline every 4 registers
        }
        System.out.println("-----------------");
    }

    public int getPC() {
        return this.pc;
    }

    public int getRegister(int index) {
        if (index >= 0 && index < 16) {
            return this.vRegister[index];
        }
        return -1;
    }

    public void loadROM(String filePath) {
        if (filePath.isEmpty()){
            throw new NullPointerException("file path is empty CHECK MAIN.JAVA AT LINE 35");
        }
        else {
            try {
                byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
                System.out.println("File successfully read into byte array. Size: " + fileBytes.length + " bytes.");
                for (int i = 0; i < Math.min(10, fileBytes.length); i++) {
                    System.out.print(fileBytes[i] + " ");
                }
                System.out.println();
                for (int i = 0; i < fileBytes.length; i++) {
                    memory[512 + i] = fileBytes[i] & 0xFF;
                }
                System.out.println("Loaded " + fileBytes.length + " bytes.");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }

    public void setKey(int key, boolean isPressed) {
        if (key >= 0 && key < 16) {
            keys[key] = isPressed;
        }
    }
    public int getSoundTimer() {
        return this.soundTimer;
    }

}
