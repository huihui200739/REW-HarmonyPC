package ohos.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;

/**
 * A TargetDataLine backed by OH_AudioCapturer (OHAudio).
 *
 * <p>Captured audio is pushed into a native ring buffer by the capturer callback;
 * {@link #read(byte[], int, int)} drains it. If the native side is unavailable the line
 * returns silence so callers keep running.</p>
 */
public final class OhosTargetDataLine implements TargetDataLine {

    private final AudioFormat format;
    private final Line.Info info;
    private boolean open;
    private boolean running;
    private int bufferSize = 8192;
    private long position;
    private long handle;

    private static native long nativeOpen(int sampleRate, int channels, int bits);
    private static native int nativeRead(long handle, byte[] data, int off, int len);
    private static native void nativeClose(long handle);

    public OhosTargetDataLine(AudioFormat format) {
        this.format = format;
        this.info = new DataLine.Info(TargetDataLine.class, format);
    }

    @Override public void open(AudioFormat fmt, int bufferSize) { this.bufferSize = bufferSize; open(); }
    @Override public void open(AudioFormat fmt) { open(); }
    @Override public void open() {
        open = true;
        try {
            handle = nativeOpen((int) format.getSampleRate(),
                                format.getChannels(),
                                format.getSampleSizeInBits());
        } catch (Throwable ignored) {
            handle = 0;
        }
    }

    @Override public int read(byte[] b, int off, int len) {
        if (handle != 0) {
            try {
                int n = nativeRead(handle, b, off, len);
                position += Math.max(0, n);
                return n;
            } catch (Throwable ignored) {
                // fall through to silence
            }
        }
        java.util.Arrays.fill(b, off, off + len, (byte) 0);
        position += len;
        return len;
    }

    @Override public void flush() { }
    @Override public void drain() { }
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

    @Override public void close() {
        open = false;
        running = false;
        if (handle != 0) {
            try {
                nativeClose(handle);
            } catch (Throwable ignored) {
                // ignore
            }
            handle = 0;
        }
    }

    @Override public boolean isOpen() { return open; }
    @Override public Control[] getControls() { return new Control[0]; }
    @Override public boolean isControlSupported(Control.Type control) { return false; }
    @Override public Control getControl(Control.Type control) {
        throw new IllegalArgumentException("No control: " + control);
    }
    @Override public void addLineListener(LineListener listener) { }
    @Override public void removeLineListener(LineListener listener) { }
}
