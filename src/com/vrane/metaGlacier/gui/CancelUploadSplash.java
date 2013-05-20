/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import java.util.logging.Level;

/**
 *
 * @author kz
 */
class CancelUploadSplash extends Splash{

    UploadThread.UploadOneThread upload_thread;
    
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
