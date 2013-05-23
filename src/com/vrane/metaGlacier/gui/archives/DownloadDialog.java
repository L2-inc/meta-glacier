/*
 * @(#)DownloadDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacier.gui.archives.DownloadArchive.DownloadThread;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JDialog;

/**
 * This is the window dedicated to displaying the progress of uploading task.
 * @author K Z Win
 */
public class DownloadDialog extends JDialog{

    private final static Logger LGR = Main.getLogger(DownloadDialog.class);
    
    private DownloadThread download_thread;
    
    DownloadDialog(final Archive archive, final File sel) {
        FileProgressPanel fpp = new FileProgressPanel();
        archive.withProgressReporter(fpp);
        add(fpp);
        pack();
        setResizable(false);
        setLocationRelativeTo(Main.frame);
        addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent we) {
                LGR.info("cancelling job");
                download_thread.interrupt();
            }
        });
        fpp.resetCurrentFilePb(sel.getName(),
                HumanBytes.convert(archive.getSize()));
    }
    
    
    void withWorker(DownloadThread dt) {
        download_thread = dt;
    }

}
