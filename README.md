# Chip8: A CHIP-8 Emulator in Java

This is a simple CHIP-8 emulator written from scratch in Java. It was built as a learning project to understand the core concepts of CPU emulation, memory, I/O, and hardware simulation.

The emulator is capable of loading and running most original CHIP-8 ROMs, such as Pong, Space Invaders, and various tech demos.

## Emulator Features

This project implements all core components of the CHIP-8 virtual machine:

* **CPU:** A complete 35-instruction 8-bit CPU, including all math, logic, branching, and subroutine opcodes.
* **Memory:** 4KB of addressable memory.
* **Registers:** 16 general-purpose 8-bit `V` registers, a 16-bit `I` register, and a program counter (`pc`).
* **Graphics:** A 64x32 monochrome display, rendered to a Java Swing `JPanel`. Implements the `Dxyn` (draw) opcode with sprite XORing and collision detection (`VF` flag).
* **Timers:** A 60Hz `delayTimer` and `soundTimer`.
* **Sound:** Real-time audio generation (a 440Hz sine wave) when the `soundTimer` is active.
* **Input:** A 16-key hexadecimal keypad, mapped to the standard QWERTY keyboard.
* **ROM Loading:** Loads CHIP-8 ROM files (`.ch8`) directly into memory.

## How to Run

1.  **Prerequisites:** You must have a Java JDK (e.g., JDK 21) installed and configured.
2.  **Compile:** Compile all the Java files.
    ```bash
    javac Main.java cpu/Chip8.java
    ```
3.  **Run:** Run the `Main` class.
    ```bash
    java Main
    ```
**OR JUST USIN INTELLIJ!!!!**


4.  **Place ROMs:** The emulator looks for ROMs in a `ROMS/` folder in the project's root directory. The `Main.java` file is currently hard-coded to load `ROMS/Pong (alt).ch8`.

## Default Keypad Controls

The 16-key CHIP-8 keypad is mapped to the left side of a QWERTY keyboard:

| CHIP-8 Key | Keyboard Key |
| :--- | :--- |
| `1` `2` `3` `C` | `1` `2` `3` `4` |
| `4` `5` `6` `D` | `Q` `W` `E` `R` |
| `7` `8` `9` `E` | `A` `S` `D` `F` |
| `A` `0` `B` `F` | `Z` `X` `C` `V` |

For *Pong*, Player 1 uses `1` (Up) and `Q` (Down). Player 2 uses `4` (Up) and `R` (Down).

## Project Structure

* `Main.java`: The main entry point. Handles creating the `JFrame` window, the `ScreenPanel` (our renderer), the keyboard input (`KeyListener`), the sound output (`SourceDataLine`), and the main 60Hz game loop.
* `cpu/Chip8.java`: The core emulator. This class contains all the "hardware" (memory, registers, stack, timers) and the main `cycle()` method, which implements the Fetch-Decode-Execute loop for all 35 opcodes.
* `ROMS/`: A directory containing the CHIP-8 game files.
