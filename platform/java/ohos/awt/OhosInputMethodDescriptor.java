package ohos.awt;

import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

/** Descriptor so the AWT input method framework can instantiate {@link OhosInputMethod}. */
public final class OhosInputMethodDescriptor implements InputMethodDescriptor {

    @Override public Locale[] getAvailableLocales() {
        return new Locale[] { Locale.getDefault() };
    }

    @Override public boolean hasDynamicLocaleList() {
        return false;
    }

    @Override public String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
        return "OHOS Input Method";
    }

    @Override public java.awt.Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    @Override public InputMethod createInputMethod() {
        return new OhosInputMethod();
    }
}
