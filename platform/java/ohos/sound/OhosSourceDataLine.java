package ohos.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * A SourceDataLine that accepts writes and discards them.
 *
 * <p>Lets REW open an output line and run its own state machine without a real audio backend.
 * Audio is not audible; writes return immediately.</p>
 */
public final class OhosSourceDataLine implements SourceDataLine {

    private final AudioFormat format;
    private final Line.Info info;
    private boolean open;
    private boolean running;
    private int bufferSize = 8192;
    private long position;

    public OhosSourceDataLine(AudioFormat format) {
        this.format = format;
        this.info = new DataLine.Info(SourceDataLine.class, format);
    }

    @Override public void open(AudioFormat fmt, int bufferSize) { this.bufferSize = bufferSize; open = true; }
    @Override public void open(AudioFormat fmt) { open = true; }
    @Override public void open() { open = true; }
    @Override public int write(byte[] b, int off, int len) {
        position += len;
        return len;
    }
    @Override public void drain() { }
    @Override public void flush() { }
    @Override public void start() { running = true; }
    @Override public void stop() { running = false; }
    @Override public boolean isRunning() { return running; }
    @Override public boolean isActive() { return running; }
    @Override public AudioFormat getFormat() { return format; }
    @Override public int getBufferSize() { return bufferSize; }
    @Override public int available() { return bufferSize; }
    @Override public int getFramePosition() { return (int) position; }
    @Override public long getLongFramePosition() { return position; }
    @Override public long getMicrosecondPosition() {
        return (long) (position * 1_000_000.0 / Math.max(1, format.getFrameSize() * format.getFrameRate()));
    }
    @Override public float getLevel() { return AudioSystem.NOT_SPECIFIED; }
    @Override public Line.Info getLineInfo() { return info; }
    @Override public void close() { open = false; running = false; }
    @Override public boolean isOpen() { return open; }
    @Override public Control[] getControls() { return new Control[0]; }
    @Override public boolean isControlSupported(Control.Type control) { return false; }
    @Override public Control getControl(Control.Type control) {
        throw new IllegalArgumentException("No control: " + control);
    }
    @Override public void addLineListener(LineListener listener) { }
    @Override public void removeLineListener(LineListener listener) { }
}
