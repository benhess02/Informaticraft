package io.github.benhess02.informaticraft.util;

import java.util.Arrays;

public class DisplayBuffer {
    protected int width = 0;
    protected int height = 0;

    protected char[] text = new char[0];
    protected int[] foreground = new int[0];
    protected int[] background = new int[0];

    protected int cursorX = 0;
    protected int cursorY = 0;
    protected boolean cursorVisible = true;

    public DisplayBuffer(int width, int height) {
        resize(width, height);
    }

    public void resize(int newWidth, int newHeight) {
        if(newWidth < 0 || newHeight < 0) {
            throw new RuntimeException("Invalid display buffer size.");
        }
        if(newWidth == width && newHeight == height) {
            return;
        }
        char[] newText = new char[newWidth * newHeight];
        int[] newForeground = new int[newWidth * newHeight];
        int[] newBackground = new int[newWidth * newHeight];
        Arrays.fill(newText, ' ');
        Arrays.fill(newForeground, (255 << 16) | (255 << 8) | 255);
        if(text != null) {
            int copyWidth = Math.min(width, newWidth);
            int copyHeight = Math.min(height, newHeight);
            for(int y = 0; y < copyHeight; y++) {
                for(int x = 0; x < copyWidth; x++) {
                    int index = width * y + x;
                    int newIndex = newWidth * y + x;
                    newText[newIndex] = text[index];
                    newForeground[newIndex] = foreground[index];
                    newBackground[newIndex] = background[index];
                }
            }
        }
        width = newWidth;
        height = newHeight;
        text = newText;
        foreground = newForeground;
        background = newBackground;
    }

    public String getTextBuffer() {
        return String.copyValueOf(text);
    }

    public void setTextBuffer(String text) {
        if(text.length() != this.text.length) {
            throw new RuntimeException("Text buffer size does not match");
        }
        for(int i = 0; i < text.length(); i++) {
            this.text[i] = text.charAt(i);
        }
    }

    public int[] getBackgroundBuffer() {
        return background.clone();
    }

    public void setBackgroundBuffer(int[] background) {
        if(background.length != this.background.length) {
            throw new RuntimeException("Background buffer size does not match");
        }
        System.arraycopy(background, 0, this.background, 0, background.length);
    }

    public int[] getForegroundBuffer() {
        return foreground.clone();
    }

    public void setForegroundBuffer(int[] foreground) {
        if(foreground.length != this.foreground.length) {
            throw new RuntimeException("Foreground buffer size does not match");
        }
        System.arraycopy(foreground, 0, this.foreground, 0, foreground.length);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public char getChar(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height) {
            return (char)0;
        }
        return text[y * width + x];
    }

    public void setChar(int x, int y, char ch) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            text[y * width + x] = ch;
        }
    }

    public int getBackground(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        return background[y * width + x];
    }

    public void setBackground(int x, int y, int color) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            background[y * width + x] = color;
        }
    }

    public int getForeground(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        return foreground[y * width + x];
    }

    public void setForeground(int x, int y, int color) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            foreground[y * width + x] = color;
        }
    }

    public int getCursorX() {
        return cursorX;
    }

    public void setCursorX(int cursorX) {
        if(width > 0) {
            this.cursorX = Math.clamp(cursorX, 0, width - 1);
        }
    }

    public void setCursorY(int cursorY) {
        if(height > 0) {
            this.cursorY = Math.clamp(cursorY, 0, height - 1);
        }
    }

    public int getCursorY() {
        return cursorY;
    }

    public void setCursor(int x, int y) {
        setCursorX(x);
        setCursorY(y);
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    public void setCursorVisible(boolean cursorVisible) {
        this.cursorVisible = cursorVisible;
    }
}
