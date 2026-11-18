package ohos.sound;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.MixerProvider;

/**
 * Minimal OHOS MixerProvider.
 *
 * <p>Exposes a virtual mixer so device enumeration is non-empty. Actual audio I/O is not
 * implemented yet; lines are advertised but opening them throws LineUnavailableException.</p>
 */
public final class OhosMixerProvider extends MixerProvider {

    private static final List<Mixer.Info> INFOS = new ArrayList<>();
    private static final List<Mixer> MIXERS = new ArrayList<>();

    static {
        INFOS.add(new OhosMixerInfo("OHOS Audio Out", "OHOS", "OHOS audio output (stub)"));
        INFOS.add(new OhosMixerInfo("OHOS Audio In", "OHOS", "OHOS audio input (stub)"));
        for (Mixer.Info info : INFOS) {
            MIXERS.add(new OhosMixer(info));
        }
    }

    @Override
    public Mixer.Info[] getMixerInfo() {
        return INFOS.toArray(new Mixer.Info[0]);
    }

    @Override
    public Mixer getMixer(Mixer.Info info) {
        if (info == null) {
            return MIXERS.get(0);
        }
        for (int i = 0; i < INFOS.size(); i++) {
            if (INFOS.get(i).equals(info)) {
                return MIXERS.get(i);
            }
        }
        throw new IllegalArgumentException("Unknown mixer: " + info);
    }

    static final class OhosMixerInfo extends Mixer.Info {
        OhosMixerInfo(String name, String vendor, String description) {
            super(name, vendor, description, "1.0");
        }
    }

    static final class OhosMixer implements Mixer {
        private final Info info;

        OhosMixer(Info info) {
            this.info = info;
        }

        @Override public Info getMixerInfo() { return info; }
        @Override public Line.Info[] getSourceLineInfo() { return new Line.Info[0]; }
        @Override public Line.Info[] getTargetLineInfo() { return new Line.Info[0]; }
        @Override public Line.Info[] getSourceLineInfo(Line.Info info) { return new Line.Info[0]; }
        @Override public Line.Info[] getTargetLineInfo(Line.Info info) { return new Line.Info[0]; }
        @Override public boolean isLineSupported(Line.Info info) {
            if (info == null) {
                return false;
            }
            Class<?> lc = info.getLineClass();
            return javax.sound.sampled.SourceDataLine.class.isAssignableFrom(lc)
                    || javax.sound.sampled.TargetDataLine.class.isAssignableFrom(lc);
        }
        @Override public Line getLine(Line.Info info) throws LineUnavailableException {
            if (info != null && javax.sound.sampled.SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
                AudioFormat fmt = null;
                if (info instanceof DataLine.Info) {
                    AudioFormat[] fmts = ((DataLine.Info) info).getFormats();
                    if (fmts.length > 0) {
                        fmt = fmts[0];
                    }
                }
                if (fmt == null) {
                    fmt = new AudioFormat(48000f, 16, 2, true, false);
                }
                return new OhosSourceDataLine(fmt);
            }
            if (info != null && javax.sound.sampled.TargetDataLine.class.isAssignableFrom(info.getLineClass())) {
                AudioFormat fmt = null;
                if (info instanceof DataLine.Info) {
                    AudioFormat[] fmts = ((DataLine.Info) info).getFormats();
                    if (fmts.length > 0) {
                        fmt = fmts[0];
                    }
                }
                if (fmt == null) {
                    fmt = new AudioFormat(48000f, 16, 1, true, false);
                }
                return new OhosTargetDataLine(fmt);
            }
            throw new LineUnavailableException("OHOS line not supported: " + info);
        }
        @Override public int getMaxLines(Line.Info info) { return 0; }
        @Override public Line[] getSourceLines() { return new Line[0]; }
        @Override public Line[] getTargetLines() { return new Line[0]; }
        @Override public void synchronize(Line[] lines, boolean maintainSync) { }
        @Override public void unsynchronize(Line[] lines) { }
        @Override public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
            return false;
        }
        @Override public Line.Info getLineInfo() { return new Line.Info(Mixer.class); }
        @Override public void open() { }
        @Override public void close() { }
        @Override public boolean isOpen() { return true; }
        @Override public Control[] getControls() { return new Control[0]; }
        @Override public boolean isControlSupported(Control.Type control) { return false; }
        @Override public Control getControl(Control.Type control) {
            throw new IllegalArgumentException("No control: " + control);
        }
        @Override public void addLineListener(LineListener listener) { }
        @Override public void removeLineListener(LineListener listener) { }
    }
}
