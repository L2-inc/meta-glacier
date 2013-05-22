/*
 * @(#)ASplash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.vaults;

import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.vrane.metaGlacier.AllVaults;
import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Vault;
import com.vrane.metaGlacier.VaultInventory;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacier.gui.archives.ArchiveManageDialog;
import com.vrane.metaGlacier.gui.utilities.Splash;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.ArchiveRO;
import com.vrane.metaGlacierSDK.MArchive;
import com.vrane.metaGlacierSDK.MArchiveList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This splash panel is displayed when a list of archives in a vault is to be displayed.
 * A splash screen is displayed whenever a long running networking operation might freeze
 * the UI.  This splash screen gets a list of archives of a vault from AWS in the background.
 * Additionally it also parses a list of ready download jobs.
 * 
 * @author K Z Win
 */
class ASplash extends Splash{
    private final static Logger LGR = Main.getLogger(ASplash.class);
    
    long now;
    private String vaultName;
    ArrayList<Archive> Archives = null;
    boolean noData = false;
    int job_count = 0;
    ArrayList<MArchive> ExtraArchives = new ArrayList();


    ASplash(String vault_name) {
        vaultName = vault_name;
        get_data();
    }
    
    private void get_data(){
        say("Getting data from AWS for " + vaultName);
        Vault vaultObj = new Vault(vaultName);
        List<GlacierJobDescription> jobs = vaultObj.listJobs();
        Calendar cal = new GregorianCalendar();
        
        // The following date will always be in the past for this software
        cal.set(2012, 0, 01, 1, 1, 1);
        Date mostRecent = cal.getTime();
        Date invDate = null;
        LGR.log(Level.INFO, "number of jobs to process {0}", jobs.size());
        for (final GlacierJobDescription j: jobs) {
            final String jobId = j.getJobId();
            GetJobOutputResult jor = null;
            
            LGR.log(Level.FINE, "Getting data for {0}", jobId);
            try {
                jor = vaultObj.getJobOutput(jobId);
            } catch (Exception e) {
                LGR.log(Level.SEVERE, null, e);
                continue;
            }
            final String jobContentType = jor.getContentType();
            if (jobContentType == null) {
                /* XXX why getMessage() returns null with this */
                LGR.log(Level.SEVERE, "Job content-type is null");
                continue;
            }
            LGR.log(Level.FINE, "job content type {0}", jobContentType);
            
            if (jor.getContentType().equals("application/octet-stream")) {
                /* This means archive download job is ready */
                AllVaults.addDownloadableArchiveJobId(vaultName, jobId);
                LGR.log(Level.FINE, "number of archive jobs found {0}",
                        ++job_count);
                continue;
            } // Else this might be the inventory job
            VaultInventory vi = null;
            try {
                vi = new VaultInventory(jor, vaultName);
            } catch (Exception ex) {
                LGR.log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Error getting vaults");
                return;
            }
            invDate = vi.getInventoryDate();
            LGR.log(Level.FINE, "inventory date {0}", invDate);
            if (invDate.compareTo(mostRecent) < 0) {
                continue;
            }
            mostRecent = invDate;
            LGR.fine("Now getting archive list from AWS");
            Archives = vi.getArchives();
            LGR.log(Level.FINE, "Number of archives {0}", Archives.size());
        }
        final boolean noAWSdata = jobs.isEmpty() || Archives == null;
        if (noAWSdata) {
            if (!GlacierFrame.haveMetadataProvider()) {
                dispose();
                JOptionPane.showMessageDialog(null, "No job data from AWS");
                return;
            } else if (!Main.frame.doNotConfirmGettingMetadata()) {
                final int selectedButton = JOptionPane.showConfirmDialog(
                        this,
                        "Get data from metadata provider?",
                        "No AWS Job Ready",
                        JOptionPane.YES_NO_OPTION);
                if (selectedButton != JOptionPane.YES_OPTION) {
                    dispose();
                    return;
                }
            }
        }
        if (GlacierFrame.haveMetadataProvider()) {
            say("Getting data from metadata provider");
            MArchiveList mal = new MArchiveList(new Vault(vaultName));
            if (Archives != null) {
                for (Archive a: Archives) {
                    ArchiveRO ma = new ArchiveRO(a.getArchiveId(),
                        a.getSize(), a.getDescription());
                    mal.add(ma.withAWSCreateTime(a.getAWSCreateTime()));
                }
            }
            boolean metadataError = true;
            try {
                if (mal.sync()) {
                    for (final MArchive mar: mal.getArchiveList()) {
                        ExtraArchives.add(mar);
                    }
                    metadataError = false;
                }
            } catch (SDKException e) {
                LGR.log(Level.SEVERE, null, e);
            } catch (Exception ex) {
                LGR.log(Level.SEVERE, null, ex);
            }
            if (metadataError) {
                JOptionPane.showMessageDialog(this,
                        "Error from metadata provider!");
                dispose();
            }
        }
        if (noAWSdata && ExtraArchives.isEmpty()) {
            dispose();
            JOptionPane.showMessageDialog(this, "No data retrieved");
            return;
        }
        ArchiveManageDialog amd = new ArchiveManageDialog(vaultName, Archives,
                ExtraArchives,
                false, "inventory for '" + vaultName + "'");
        dispose();
        amd.setVisible(true);

    }
}
