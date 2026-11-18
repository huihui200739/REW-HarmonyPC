package ohos.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Locale;

/**
 * Minimal InputMethod: passes committed text straight through to the client component.
 *
 * <p>OHOS IME integration would need a native bridge to the system input method; this
 * implementation keeps Swing text components functional with direct (ASCII) typing and lets
 * {@code dispatchEvent} deliver committed characters, which is enough for REW's numeric and
 * file-name fields.</p>
 */
public final class OhosInputMethod implements InputMethod {

    private InputMethodContext context;
    private Locale locale = Locale.getDefault();

    @Override public void setInputMethodContext(InputMethodContext context) {
        this.context = context;
    }

    @Override public boolean setLocale(Locale locale) {
        this.locale = locale;
        return true;
    }

    @Override public Locale getLocale() {
        return locale;
    }

    @Override public void setCharacterSubsets(Character.Subset[] subsets) {
    }

    @Override public void setCompositionEnabled(boolean enable) {
    }

    @Override public boolean isCompositionEnabled() {
        return false;
    }

    @Override public void reconvert() {
    }

    @Override public void dispatchEvent(AWTEvent event) {
    }

    @Override public void notifyClientWindowChange(java.awt.Rectangle bounds) {
    }

    @Override public void activate() {
    }

    @Override public void deactivate(boolean isTemporary) {
    }

    @Override public void hideWindows() {
    }

    @Override public void removeNotify() {
    }

    @Override public void endComposition() {
    }

    @Override public void dispose() {
    }

    @Override public Object getControlObject() {
        return null;
    }

}
