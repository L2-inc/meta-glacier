/*
 * @(#)SearchSplash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.archives.ArchiveManageDialog;
import com.vrane.metaGlacier.gui.utilities.Splash;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.MArchive;
import com.vrane.metaGlacierSDK.Search;
import com.vrane.metaGlacierSDK.SignInException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

class SearchSplash extends Splash{
    private final static Logger LGR = Main.getLogger(SearchSplash.class);
    
    private ArrayList<Archive> Archives = null;
    private ArrayList<MArchive> archivesFromMetadataServer = new ArrayList();
    private final Search search;
    
    SearchSplash(final Search s) {
        search = s;
        get_data();
    }
    
    private void get_data(){
        boolean metadataError = true;

        say("Searching metadata provider");
        try {
            archivesFromMetadataServer = search.search();
            metadataError = false;
        } catch (SDKException | SignInException e) {
            LGR.log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(this,
                    "Error getting metadata.  Check your connection!");
            dispose();
            return;
        } catch (APIException e) {
            LGR.log(Level.SEVERE, null, e);
        }
        if (metadataError) {
            LGR.warning("Error from metadata provider");
            dispose();
            JOptionPane.showMessageDialog(this,
                    "Error from metadata provider!");
            return;
        }
        if (null == archivesFromMetadataServer) {
            dispose();
            JOptionPane.showMessageDialog(null, "You did not specify a search");
            return;
        }
        if (archivesFromMetadataServer.isEmpty()) {
            dispose();
            JOptionPane.showMessageDialog(this, "No data match your search");
            return;
        }
        ArchiveManageDialog amd = new ArchiveManageDialog(null, Archives,
                archivesFromMetadataServer,
                true, "search result");
        dispose();
        amd.setVisible(true);
    }
}
