/*
 * @(#)DownloadArchive.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Vault;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.MArchive;
import com.vrane.metaGlacierSDK.SignInException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class DownloadArchive{
    
    private final static String DOWNLOAD_FOLDER_KEY = "download folder";
    private final static String DOWNLOAD_FILE_KEY = "download file";
    private final static Preferences P =
            Preferences.userNodeForPackage(DownloadArchive.class);
    private final static Logger LGR = Main.getLogger(DownloadArchive.class);
    private final Archive archive;
    private File selection = null;
    private DownloadDialog dd;
    private boolean cancelled = false;
    
    DownloadArchive(final Archive arch){
        String fileName = null;
        archive = arch;
        
        
        if (GlacierFrame.haveMetadataProvider()) {
            final Vault v = new Vault(arch.getVaultName());
            try {
                fileName = new MArchive(v, arch.getArchiveId()).getFileName();
            } catch (SDKException | APIException | SignInException ex) {
                LGR.log(Level.SEVERE, null, ex);
                fileName = arch.getDescription();
            }
        }
        if (fileName == null || fileName.isEmpty()) {
            fileName = P.get(DOWNLOAD_FILE_KEY, "glacier_download_file");
        }
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setSelectedFile(new File(
                P.get(DOWNLOAD_FOLDER_KEY, System.getProperty("user.home")),
                    fileName));
        if (jfc.showDialog(null, "download") != JFileChooser.APPROVE_OPTION) {
            P.remove(DOWNLOAD_FILE_KEY);
            LGR.info("User cancelled file selection");
            cancelled = true;
            return;
        }
        selection = jfc.getSelectedFile();
        if (selection.exists()) {
            if(JOptionPane.CANCEL_OPTION
                    == JOptionPane.showConfirmDialog(null, 
                     "Replace this file?", "File exists",
                    JOptionPane.OK_CANCEL_OPTION)){
                cancelled = true;
                return;
            }
        }
        P.put(DOWNLOAD_FILE_KEY, selection.getName());
        P.put(DOWNLOAD_FOLDER_KEY, selection.getParent());
        dd  = new DownloadDialog(archive, selection);
    }

    boolean isCancelled(){
        return cancelled;
    }
    
    public void run(){
        DownloadThread dt = new DownloadThread();
        dd.withWorker(dt);
        dt.start();
        dd.setVisible(true);
    }
    
    class DownloadThread extends Thread {
        
        @Override
        public void run(){
            boolean success = false;
            try {
                success = archive.download(selection);
            } catch (Exception e) {
                LGR.log(Level.SEVERE, null, e);
            }
            if (!success) {
                JOptionPane.showMessageDialog(null, "Error downloading");
            }
            dd.dispose();
        }
    }
}
