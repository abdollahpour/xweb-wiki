package ir.xweb.module;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class TestWiki {

    @Test
    public void convertDir() throws IOException {
        // convert all Mediawiki and Markdown files inside of the directory
        final File dir = new File("/home/hamed/git/abdollahpour.github.io/wiki");
        if(dir.exists()) {
            final File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".md") || f.getName().endsWith(".mediawiki");
                }
            });

            if(files != null) {
                for(File f:files) {
                    final String n = f.getName();
                    if(f.getName().endsWith(".md")) {
                        WikiModule.markdownConvert(f, new File(dir, n.substring(0, n.length() - 3) + ".html"));
                    } else {
                        WikiModule.mediawikiConvert(f, new File(dir, n.substring(0, n.length() - 10) + ".html"));
                    }
                }
            }

        }
    }

}
