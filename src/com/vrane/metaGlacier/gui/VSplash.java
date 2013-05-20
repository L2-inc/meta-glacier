/*
 * @(#)VSplash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.vrane.metaGlacier.AllVaults;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import com.vrane.metaGlacier.gui.vaults.VaultManageDialog;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.VaultList;
import com.vrane.metaGlacierSDK.VaultRO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

class VSplash extends Splash{
    private final static Logger LGR = Main.getLogger(VSplash.class);
        
    VSplash(){
        get_data();
    }
    
    private void get_data(){
        say("Getting vault list from AWS");
        final long beginning = System.currentTimeMillis();
        List<DescribeVaultOutput> list = null;
        
        try {
            list = new AllVaults().list();
        } catch (Exception ex) {
            LGR.log(Level.SEVERE, null, ex);
            dispose();
            JOptionPane.showMessageDialog(null,
                "Error getting vaults.  Check your connection or AWS keys");
            return;
        }
        LGR.fine("Received vault list from AWS");
        final long afterAWS = System.currentTimeMillis();
        long afterMetadata = 0;
        boolean success = false;
        
        HashMap<String, String> lastCounts = new HashMap();
        
        if (list.size() > 0 && GlacierFrame.haveMetadataProvider()) {
            say("Syncing with metadata provider");
            VaultList vaultList = new VaultList(GlacierFrame.getAWSRegion());
            List<VaultRO> mvlList = new ArrayList();
            for (final DescribeVaultOutput dvo: list) {
                final VaultRO vro = new VaultRO(dvo.getVaultName(),
                        dvo.getNumberOfArchives(), dvo.getCreationDate(),
                        dvo.getSizeInBytes());
                mvlList.add(vro);
            }
            vaultList.setList(mvlList);
            try {
                success = vaultList.sync();
                afterMetadata = System.currentTimeMillis();
                lastCounts = vaultList.getLastArchiveCounts();
            } catch (SDKException | APIException ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
            if (!success) {
                JOptionPane.showMessageDialog(this,
                        "Error from metadata provider");
            }
        }
        dispose();
        if (!success) {
            return;
        }
        new VaultManageDialog(list, afterAWS - beginning,
               afterMetadata == 0 ? 0 : ( afterMetadata - afterAWS ),
                lastCounts);
    }
}
