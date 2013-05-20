/*
 * @(#)MSplash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.ArchiveMetadata;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

class MSplash extends Splash{
    private final static Logger LGR = Main.getLogger(MSplash.class);
    
    private final ArchiveMetadata MAr;
    
    MSplash(final ArchiveMetadata mar){
        MAr = mar;
        get_all_data();
    }
  
    private void get_all_data(){
        say("Checking for any metadata");
        boolean is_empty = true;
        try {
            is_empty = MAr.isEmpty();
        } catch (SDKException | APIException e){
            LGR.log(Level.SEVERE, null, e);                
        }
        dispose();
        if (is_empty) {
            JOptionPane.showMessageDialog(null,
                    "Cannot fetch metadata for archive number "
                    + MAr.getClientArchiveId()
                    + ".\nDid you upload archive without using this provider?");
            return;
        }
        new ArchiveMetadataDialog(MAr);
    }
}
