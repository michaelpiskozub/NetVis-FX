package netvis.model;

import java.io.File;

public interface RecordingOpener {
    void openCSV(File file);
    void openPCAP(File file);
}