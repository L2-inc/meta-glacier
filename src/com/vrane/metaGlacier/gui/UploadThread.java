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
import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This supervisor thread makes preparations necessary for uploading archives.
 * It decide whether to zip up, whether to move files after uploading, whether
 * to send metadata. It will in turn launch a thread for each archive to upload.
 * 
 * @author K Z Win
 */
class UploadThread extends Thread{
    private final static Logger LGR = Main.getLogger(UploadThread.class);

    private HashMap<String, ArrayList<String>> filesToMove;
    private HashSet<String> filesToRemove = new HashSet<>();
    private HashMap<String, String> fileDesc;
    private ArrayList<File> fileList;
    private long totalSize = 0;
    private int fileCount = 0;

    private String moveDir;
    private String vaultName;
    private final String region;

    UploadThread(){
        region = GlacierFrame.getAWSRegion();
    }
    
    UploadThread withVault(String s){ 
        vaultName = s;
        return this;
    }

    UploadThread withMoveDir(String m){
        moveDir = m;
        return this;
    }
    
    void setFileDesc(final HashMap<String, String> file_desc){
        fileDesc = file_desc;
    }
    
    void setFilesToMove(final HashMap<String, ArrayList<String>> files_to_move){
        filesToMove = files_to_move;
    }
    
    void setTotalSize(final long s){
        totalSize = s;
    }
    
    void setFileCount(final int count){
        fileCount = count;
    }
    
    @Override
    public void run(){
        int currentCount = 0;
        long currentTotalSize = 0;
        boolean error = false;
        final UploadDialog upload_dialog = new UploadDialog(this,
                vaultName + ": " + region);
        
        upload_dialog.prepareNewWindow(fileCount, totalSize);
        upload_dialog.setVisible(true);
        mainloop:
        for (final File file : fileList) {
            if (0 < currentCount) {
                upload_dialog.resetRate(null);
            }                

            final Long size = file.length();

            //<editor-fold defaultstate="collapsed" desc="set up current file label">
            upload_dialog
                    .reInitCurrentFilePb(file.getName(),
                    HumanBytes.convert(size));

            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="launch a new thread to start upload"> 
            final String path = file.getAbsolutePath();
            final Archive ar = new Archive(vaultName,
                    (String) fileDesc.get(path), path, region)
                    .withGranularity(Main.frame.getGranularity())
                    .withFiles(filesToMove.get(path));
            final UploadOneThread ut = new UploadOneThread(ar);
            upload_dialog.setCurrentArchive(ar);
            ut.start();
            
            try{
                ut.join();
            } catch (InterruptedException ex) {
                LGR.severe("main worker thread is interrupted");
                ut.interrupt();
                new LaunchCancelWindow(ut).execute();
                UploadSplash.upload_splash = null;
                for (final String p: filesToRemove) {
                    deleteZip(p);
                }
                return;
            }
            if (ut.failed()) {
                /* dialog message if this is frequent */
                LGR.warning("failed to upload");
                error = true;
                break;
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="set up progress bar">
            currentTotalSize += size;
            upload_dialog.updateFileCount(++currentCount);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="update rate. update progress bar">                
            long delta = ut.getTimeToUpload();
            if (delta > 0) {
                float rate = (float) (size.floatValue() / delta
                        / HumanBytes.KILO * 1000); 
                DecimalFormat myFormatter = new DecimalFormat("####.#");
                String rateString = myFormatter.format(rate);
                upload_dialog.resetRate(rateString);
            }
            upload_dialog.setCurrentTotalSize(currentTotalSize);
            //</editor-fold>
            
            deleteZip(path);
            if (moveDir == null || moveDir.isEmpty()) {
                continue;
            }
            
            //<editor-fold defaultstate="collapsed" desc="move files">                
            ArrayList<String> mL = (ArrayList<String>) filesToMove.get(path);
            for (final String oldPath: mL) {
                Path path_to_move = Paths.get(oldPath);
                try {
                    Files.move(path_to_move,
                            Paths.get(moveDir, new File(oldPath).getName()),
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException ex) {
                    LGR.log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "Cannot move file");
                    error = true;
                    break mainloop;
                }
                LGR.log(Level.INFO, "Successfully moved {0} to folder {1}",
                        new Object[]{path_to_move.getFileName(), moveDir});
            }            
            //</editor-fold>
        }
        if (!error) {
            JOptionPane.showMessageDialog(null, "All files uploaded");
        }
        upload_dialog.dispose();
        UploadSplash.upload_splash = null;
    }
    
    private void deleteZip(final String p){
        if (filesToRemove.contains(p)) {
            LGR.log(Level.INFO, "deleting file {0}", p);
            Path path = Paths.get(p);
            try {
                Files.delete(path);
            } catch (IOException ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
        }        
    }

    void setFileList(ArrayList<File> file_list) {
        fileList = file_list;
    }

    void setFilesToDelete(HashSet<String> filesToDelete) {
        filesToRemove = filesToDelete;
    }

    private static class LaunchCancelWindow extends SwingWorker<Void, Void>{
        UploadOneThread upload_one_thread;
        
        public LaunchCancelWindow(UploadOneThread ut) {
            upload_one_thread = ut;
        }
        
        @Override
        public Void doInBackground(){
            new CancelUploadSplash(upload_one_thread);
            return null;
        }
    }
    
    class UploadOneThread extends Thread{
        
        private Archive A;
        private boolean success = false;
        
        public UploadOneThread(final Archive a){
            A = a;
        }
        
        @Override
        public void run(){
            success = A.upload();
        }
                
        long getTimeToUpload(){
            return A.getTimeToUpload();
        }
        
        boolean failed(){
            return !success;
        }
        
    }
}