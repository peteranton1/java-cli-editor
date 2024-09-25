package com.example;

import com.example.term.MacOSTerminal;
import com.example.term.Terminal;
import com.example.term.UnixTerminal;
import com.example.term.WindowsTerminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.sun.jna.Platform.isMac;
import static com.sun.jna.Platform.isWindows;

public class Viewer {

  private static final int ARROW_UP = 1000,
    ARROW_DOWN = 1001,
    ARROW_LEFT = 1002,
    ARROW_RIGHT = 1003,
    HOME = 1004,
    END = 1005,
    PAGE_UP = 1006,
    PAGE_DOWN = 1007,
    DEL = 1008;

  private static int rows = 10;
  private static int columns = 10;

  private static int cursorX = 0, cursorY = 0, offsetY = 0;

  private static List<String> content = List.of();

  private static final Terminal t = isMac() ? new MacOSTerminal()  :
    isWindows() ? new WindowsTerminal() : new UnixTerminal();

  public static void main(String[] args) throws IOException {
    // System.out.println("Hello World");
        /*System.out.println("\033[4;44;31mHello World\033[0mHello");
        System.out.println("\033[2J");
        System.out.println("\033[5H");*/


    openFile(args);
    t.enableRawMode();
    initEditor();

    while (true){
      scroll();
      refreshScreen();
      int key = readKey();
      handleKey(key);
    }

  }

  private static void scroll() {
    if (cursorY >= rows + offsetY) {
      offsetY = cursorY - rows + 1;
    }
    else if (cursorY < offsetY) {
      offsetY = cursorY;
    }
  }

  private static void openFile(String[] args) {
    if (args.length == 1) {
      String filename = args[0];
      Path path = Path.of(filename);
      if (Files.exists(path)) {
        try (Stream<String> stream = Files.lines(path)) {
          content = stream.toList();
        } catch (IOException e) {
          System.out.println("Error: "+e.getMessage());
        }
      }

    }
  }

  private static void initEditor() {
    Terminal.WindowSize windowSize = t.getWindowSize();
    columns = windowSize.columns();
    rows = windowSize.rows() - 1;
  }

  private static void refreshScreen() {
    StringBuilder builder = new StringBuilder();

    moveCursorToTopLeft(builder);
    drawContent(builder);
    drawStatusBar(builder);
    drawCursor(builder);
    System.out.print(builder);
  }

  private static void moveCursorToTopLeft(StringBuilder builder) {
    builder.append("\033[H");
  }

  private static void drawCursor(StringBuilder builder) {
    builder.append(String.format("\033[%d;%dH", cursorY - offsetY + 1, cursorX + 1));
  }

  private static void drawStatusBar(StringBuilder builder) {
    String statusMessage = "Rows: " + rows + "X:" + cursorX + " Y: " + cursorY;
    builder.append("\033[7m")
      .append(statusMessage)
      .append(" ".repeat(Math.max(0, columns - statusMessage.length())))
      .append("\033[0m");
  }

  private static void drawContent(StringBuilder builder) {
    for (int i = 0; i < rows; i++) {
      int fileI = offsetY + i;
      if (fileI >= content.size()) {
        builder.append("~");
      } else {
        builder.append(content.get(fileI));
      }
      builder.append("\033[K\r\n");
    }
  }


  private static int readKey() throws IOException {
    int key = System.in.read();
    if (key != '\033') {
      return key;
    }

    int nextKey = System.in.read();
    if (nextKey != '[' && nextKey != 'O') {
      return nextKey;
    }

    int yetAnotherKey = System.in.read();

    if (nextKey == '[') {
      return switch (yetAnotherKey) {
        case 'A' -> ARROW_UP;  // e.g. esc[A == arrow_up
        case 'B' -> ARROW_DOWN;
        case 'C' -> ARROW_RIGHT;
        case 'D' -> ARROW_LEFT;
        case 'H' -> HOME;
        case 'F' -> END;
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
          int yetYetAnotherChar = System.in.read();
          if (yetYetAnotherChar != '~') {
            yield yetYetAnotherChar;
          }
          switch (yetAnotherKey) {
            case '1':
            case '7':
              yield HOME;
            case '3':
              yield DEL;
            case '4':
            case '8':
              yield END;
            case '5':
              yield PAGE_UP;
            case '6':
              yield PAGE_DOWN;
            default: yield yetAnotherKey;
          }
        }
        default -> yetAnotherKey;
      };
    } else  { //if (nextKey == 'O') {  e.g. escpOH == HOME
      return switch (yetAnotherKey) {
        case 'H' -> HOME;
        case 'F' -> END;
        default -> yetAnotherKey;
      };
    }
  }

  private static void handleKey(int key) {
    if (key == 'q') {
      exit();
    }
    else if (List.of(ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, HOME, END).contains(key)) {
      moveCursor(key);
    }
        /*else {
            System.out.print((char) + key + " -> (" + key + ")\r\n");
        }*/
  }

  private static void exit() {
    System.out.print("\033[2J");
    System.out.print("\033[H");
    t.disableRawMode();
    System.exit(0);
  }

  private static void moveCursor(int key) {
    switch (key) {
      case ARROW_UP -> {
        if (cursorY > 0) {
          cursorY--;
        }
      }
      case ARROW_DOWN -> {
        if (cursorY < content.size()) {
          cursorY++;
        }
      }
      case ARROW_LEFT -> {
        if (cursorX > 0) {
          cursorX--;
        }
      } case ARROW_RIGHT -> {
        if (cursorX < columns - 1) {
          cursorX++;
        }
      }
      case HOME -> cursorX = 0;
      case END -> cursorX = columns - 1;
    }
  }

}



/**/

