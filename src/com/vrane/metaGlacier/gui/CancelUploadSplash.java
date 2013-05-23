/*
 * @(#)CancelUploadSplash.java  0.7 2013 May 23
 *
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 *
 * See LICENSE file accompanying this file.
 */package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import java.util.logging.Level;

/**
 * A splash window to show when the user cancel the upload.
 * 
 * @author K Z Win
 */
class CancelUploadSplash extends Splash{

    UploadThread.UploadOneThread upload_thread;

    /**
     * Sole constructor taking a cancelled upload thread object.
     * 
     * @param ut the thread object representing the upload job of an archive.
     */
    public CancelUploadSplash(UploadThread.UploadOneThread ut) {
        
        say("canceling current upload");
        upload_thread = ut;
        doWork();
    }
    
    private void doWork(){
        try{
            upload_thread.join();
        } catch (InterruptedException e) {
            Main.getLogger(CancelUploadSplash.class).log(Level.SEVERE, null, e);
        }
        dispose();
    }
    
}
