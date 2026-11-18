package ohos.awt;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** In-process Clipboard. Contents live in memory and reset when the app exits. */
public final class OhosClipboard extends Clipboard {

    private static final long serialVersionUID = 1L;

    private Transferable contents;

    public OhosClipboard(String name) {
        super(name);
    }

    @Override
    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        this.contents = contents;
    }

    @Override
    public synchronized Transferable getContents(Object requestor) {
        final Transferable c = contents;
        if (c != null) {
            return c;
        }
        return new Transferable() {
            @Override public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.stringFlavor };
            }
            @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.stringFlavor.equals(flavor);
            }
            @Override public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException {
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return "";
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }

    @Override
    public synchronized DataFlavor[] getAvailableDataFlavors() {
        Transferable c = contents;
        return c != null ? c.getTransferDataFlavors() : new DataFlavor[] { DataFlavor.stringFlavor };
    }

    @Override
    public synchronized boolean isDataFlavorAvailable(DataFlavor flavor) {
        Transferable c = contents;
        return c != null && c.isDataFlavorSupported(flavor);
    }

    @Override
    public synchronized Object getData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        return getContents(null).getTransferData(flavor);
    }
}
