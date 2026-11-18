package ohos.awt;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * PrinterJob that renders to an image instead of a printer.
 *
 * <p>REW's "print graph" goes through {@code PrinterJob.print()}. OHOS has no CUPS/print
 * service, so this implementation renders every page into a PNG under the user's home
 * (or /tmp) directory and reports success. That keeps the menu item functional and gives
 * the user a usable artifact.</p>
 */
public final class OhosPrinterJob extends PrinterJob {

    private Printable printable;
    private Pageable pageable;
    private PageFormat pageFormat = new PageFormat();
    private String jobName = "REW job";

    @Override public void setPrintable(Printable painter) {
        this.printable = painter;
        this.pageable = null;
    }

    @Override public void setPrintable(Printable painter, PageFormat format) {
        this.printable = painter;
        this.pageFormat = format;
        this.pageable = null;
    }

    @Override public void setPageable(Pageable document) throws NullPointerException {
        this.pageable = document;
        this.printable = null;
    }

    @Override public void setCopies(int copies) {
    }

    @Override public int getCopies() {
        return 1;
    }

    @Override public String getJobName() {
        return jobName;
    }

    @Override public String getUserName() {
        return System.getProperty("user.name", "ohos");
    }

    @Override public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override public void print() throws PrinterException {
        try {
            String base = System.getProperty("user.home", "/tmp");
            if (pageable != null) {
                int n = pageable.getNumberOfPages();
                for (int i = 0; i < n; i++) {
                    renderPage(pageable.getPrintable(i), pageable.getPageFormat(i), i, base);
                }
            } else if (printable != null) {
                renderPage(printable, pageFormat, 0, base);
            }
        } catch (Throwable t) {
            throw new PrinterException("OHOS print render failed: " + t);
        }
    }

    private void renderPage(Printable p, PageFormat fmt, int index, String base) throws Exception {
        int w = (int) Math.max(1, fmt.getImageableWidth());
        int h = (int) Math.max(1, fmt.getImageableHeight());
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, w, h);
        p.print(g, fmt, index);
        g.dispose();
        java.io.File out = new java.io.File(base,
                "rew-print-" + System.currentTimeMillis() + "-" + index + ".png");
        javax.imageio.ImageIO.write(img, "png", out);
        System.err.println("[OHOS-PRINT] rendered page " + index + " -> " + out);
    }



    @Override public PageFormat defaultPage(PageFormat page) {
        return (PageFormat) page.clone();
    }

    @Override public PageFormat validatePage(PageFormat page) {
        return page;
    }

    @Override public boolean printDialog() {
        return true;
    }

    @Override public PageFormat pageDialog(PageFormat page) {
        return (PageFormat) page.clone();
    }

    @Override public PrintService getPrintService() {
        return null;
    }

    @Override public void setPrintService(PrintService service) {
    }

    @Override public void cancel() {
    }

    @Override public boolean isCancelled() {
        return false;
    }
}
