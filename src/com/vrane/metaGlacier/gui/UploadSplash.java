/*
 * @(#)UploadThread.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This thread make all preparation necessary for uploading archives.
 * It decide whether to zip up, whether to move files after uploading, whether
 * to send metadata.
 * It will in turn launch a thread for each archive to upload.
 *
 * @author K Z Win
 */
class UploadSplash extends Splash{
    static UploadSplash upload_splash = null;
    private final static Logger LGR = Main.getLogger(UploadSplash.class);

    private Long sleepMin;
    private ArrayList<File> upload_list;
    private UploadThread upload_thread;

    static UploadSplash getInstance(final ArrayList<File> _list, final long sleep,
            final UploadThread ut){
        if (upload_splash == null) {
            LGR.info("creating a new window");
            upload_splash = new UploadSplash(_list, sleep, ut);
        }
        return upload_splash;
    }
    
    private UploadSplash(){}
    
    private UploadSplash(final ArrayList<File> flist, final long sleep,
            final UploadThread ut){
        sleepMin = sleep;
        upload_list = flist;
        upload_thread = ut;
        addWindowListener(new WindowAdapter(){
            
            @Override
            public void windowClosing(WindowEvent e){
                LGR.info("cancelling creating zip files");
                upload_thread = null;
                UploadSplash.this.dispose();
            }
        });
        doWork();
    }
    
    private void doWork(){
        int fileCount = 0;
        long totalSize = 0;
        final ArrayList<File> fileList = new ArrayList<>();
        final HashMap fileDesc = new HashMap();
        final HashMap<String, ArrayList<String>> filesToMove
                = new HashMap<>();
        final HashMap<String, File> filesToDelete = new HashMap<>();
        
        if (sleepMin != 0) {
            say("waiting for " + sleepMin + " minute" + 
                    (sleepMin == 1 ? "" : "s"));
        }
        try {
            Thread.sleep(sleepMin * 1000L * 60);
        } catch (InterruptedException ex) {
            LGR.log(Level.SEVERE, null, ex);
            return;
        }

        int zipSize = MainPanel.getZipSize();
        //<editor-fold defaultstate="collapsed" desc="get a list of zip or regular files to process">
        String descString = MainPanel.getDescriptionText();
        if (zipSize > 0) {
            String zipName = "meta-glacier";
            ZipArchives zAr
                    = new ZipArchives(upload_list,
                        zipSize * HumanBytes.MEGA, zipName);
            HashMap<String,ArrayList<String>> zipList = zAr.zipList;
            for (Map.Entry<String,ArrayList<String>> e: zipList.entrySet()) {
                LGR.finer("file list");
                String zipPath = "";
                say("making zip file " + ++fileCount);
                final Zip z = new Zip(e.getKey());

                try {
                    zipPath = z.zip(e.getValue().toArray(new String[0]));
                } catch (IOException ex) {
                    LGR.log(Level.SEVERE, null, ex);
                    return;
                }
                
                File zipFile = new File(zipPath);
                fileList.add(zipFile);
                totalSize += zipFile.length();

                if (descString == null) {                     
                    final Date lowDate
                            = new Date(zAr.getLowModTime(e.getKey()));
                    final Date highDate
                            = new Date(zAr.getHighModTime(e.getKey())); 
                    fileDesc.put(zipPath,
                            e.getValue().size() + " files. "
                            + " newest file " + highDate + " oldest file "
                            + lowDate);
                } else {
                    fileDesc.put(zipPath, descString);
                }
                ArrayList<String> moveList = e.getValue();
                filesToMove.put(zipPath, moveList);
                filesToDelete.put(zipPath, zipFile);
            }
        } else {           
            for (final File f: upload_list) {
                final String p = f.getAbsolutePath();
                ArrayList<String> moveList = new ArrayList<>();

                say("adding file " + fileCount);
                fileCount++;
                totalSize += f.length();
                fileDesc.put(p,
                        (descString == null) ? f.getName() : descString);
                moveList.add(p);
                filesToMove.put(p, moveList);
            }
            fileList.addAll(upload_list);
        }
        //</editor-fold>

        dispose();
        upload_thread.setFilesToDelete(filesToDelete);
        upload_thread.setFileDesc(fileDesc);
        upload_thread.setFileList(fileList);
        upload_thread.setFilesToMove(filesToMove);
        upload_thread.setTotalSize(totalSize);
        upload_thread.setFileCount(fileCount);
        upload_thread.start();
    }
}