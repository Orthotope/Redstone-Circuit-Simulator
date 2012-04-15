package Simulator;

import java.io.File;
import javax.swing.filechooser.*;

public class FileNameExtensionFilter extends FileFilter {
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        if (f.getName().endsWith("schematic")) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return "Just .schematic files";
    }

    public FileNameExtensionFilter(String a, String b) {
        super();
    }
}

