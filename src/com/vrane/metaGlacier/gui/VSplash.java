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
import com.vrane.metaGlacierSDK.SignInException;
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
        HashMap<String, String> lastCounts = new HashMap();
        long afterMetadata = 0;
        boolean success = false;        
        
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

        if(!GlacierFrame.haveMetadataProvider()){
            success = true;
        } else if (list.size() > 0) {
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
            String error_string = null;
            try {
                success = vaultList.sync();
                afterMetadata = System.currentTimeMillis();
                lastCounts = vaultList.getLastArchiveCounts();
                if (!success) {
                    LGR.info("failed to sync");
                }
            } catch (SDKException | APIException ex) {
                LGR.log(Level.SEVERE, null, ex);
            } catch (SignInException ex){
                LGR.log(Level.SEVERE, null, ex);
                error_string = "Failed to sign-in to metadata account";
            }
            dispose();
            if (!success) {
                if (error_string == null) {
                    error_string = "Error from metadata provider";
                }
                JOptionPane.showMessageDialog(this, error_string);
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
