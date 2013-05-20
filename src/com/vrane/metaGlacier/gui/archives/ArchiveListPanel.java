/*
 * @(#)ArchiveListPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.vrane.metaGlacier.AllVaults;
import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacier.gui.utilities.ByteTextField;
import com.vrane.metaGlacier.gui.utilities.ListPanelModel;
import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import com.vrane.metaGlacierSDK.ArchiveMetadata;
import com.vrane.metaGlacierSDK.MArchive;
import com.vrane.metaGlacierSDK.MVault;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

class ArchiveListPanel extends ListPanelModel{
    private static ArrayList<String> idList;
    private static Map<String,Object> downloadReadyArchives =
            new HashMap<>();
    private final static Logger LGR =
            Main.getLogger(ArchiveListPanel.class);
    
    private ArrayList<MArchive> ExtraArchives = new ArrayList<>();
    private ArrayList<Archive> archiveList;
    private long totalSize = 0;
    private static String vaultName;
    private final HashMap<String, Archive> idMap = new HashMap();
    private boolean isSearchData = false;

    ArchiveListPanel(final String vault){
        vaultName = vault;
    }
    
    ArchiveListPanel withList(ArrayList<Archive> l){
        archiveList = l;
        idList = new ArrayList();
        return this;
    }

    ArchiveListPanel withSearchFlag(final boolean searchResult) {
        isSearchData = searchResult;
        return this;
    }
    
    /**
     * This takes a list of jobs for archive download and filter out those 
     * ready for download.
     */    
    void makeArchiveJobList() throws Exception{
        final List<DescribeJobResult> djr = new ArrayList<>();
        final Set<String> vaultNames = new HashSet<>();
        
        if (vaultName == null) {
            for (final MArchive ma: ExtraArchives) {
                vaultNames.add(ma.getVaultName());
            }
        } else {
            vaultNames.add(vaultName);
        }
        LGR.log(Level.INFO, "distinct number of vaults {0}", vaultNames.size());
        for (final String vault_name: vaultNames) {
            for (final String jid: AllVaults.getDownloadJobIds(vault_name)) {
                final DescribeJobRequest djres
                        = new DescribeJobRequest()
                        .withJobId(jid).withVaultName(vault_name);
                final DescribeJobResult djresult
                        = GlacierFrame.getClient().describeJob(djres);
                djr.add(djresult);
            }
        }
        for (final DescribeJobResult res: djr) {
            downloadReadyArchives.put(res.getArchiveId(), res);
        }   
    }

    long mergedListSize(){
        return idList.size();
    }
    
    long getTotalSize(){
        return totalSize;
    }
        
    void setExtraList(final ArrayList<MArchive> extraArchives) {
        ExtraArchives = extraArchives;
        idList = new ArrayList();
    }

    void init() {
        JTextField jt;
        short count = 0;
        byte rows = 1;
        
        if (archiveList != null && idList.isEmpty()) {
            for (final Archive _a: archiveList) {
                String _id = _a.getArchiveId();
                idMap.put(_id, _a);
                idList.add(_id);
                totalSize += _a.getSize();
            }
        }
        if (ExtraArchives != null) {
            for (final MArchive a: ExtraArchives) {
                String id = a.getArchiveId();
                if (a.isDeleted() && !Main.frame.showDeleted()) {
                    LGR.log(Level.INFO, "removing deleted archive {0}", id);
                    idList.remove(id);
                    continue;
                }
                if (!idMap.containsKey(id)) {
                    idList.add(id);
                    totalSize += a.getSize();
                }
                idMap.put(id, new Archive(a));
            }
        }
        for (final String ID: idList) {
            final Archive archive = idMap.get(ID);
            count++;
            if (count < minimum || count > maximum) {
                continue;
            }
            rows++;
            //<editor-fold defaultstate="collapsed" desc="archive id label">
            final JLabel numIdLabel = new JLabel(" " + count + " ");
            numIdLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            final int clientArchiveId = count;
            numIdLabel.setToolTipText(archive.getDescription());
            if (GlacierFrame.haveMetadataProvider()) {
                numIdLabel.addMouseListener(new MouseClickListener(){

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        MVault v = new MVault(archive.getVaultName(),
                                archive.getRegion());
                        new LaunchMetadataDialog(
                            new ArchiveMetadata(v, ID)
                                .withClientArchiveId(clientArchiveId))
                                .execute();                        
                    }
                });
            }
            add(numIdLabel);
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="archive size">
            jt = new ByteTextField(archive.getSize());
            add(jt);
            //</editor-fold>

            final JButton jobButton = new JButton("Start Job");
            
            //<editor-fold defaultstate="collapsed" desc="delete button">
            final JButton deleteButton = new JButton();
            boolean in_inventory = archive.isInAWSInventory();
            boolean already_deleted = archive.deletedInAWS();
            if (in_inventory && !already_deleted) {
                deleteButton.setText("Delete");
                deleteButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (!deleteButton.isEnabled()) {
                            return;
                        }
                        deleteButton.setEnabled(false);
                        if (vaultName == null 
                                ? archive.delete() 
                                : archive.withVaultName(vaultName).delete()) {
                            deleteButton.setText("deleted");
                            jobButton.setEnabled(false);
                        } else {
                            deleteButton.setEnabled(true);
                            JOptionPane.showMessageDialog(null,
                                    archive.getDeleteError());
                        }
                    }
                
                });
            } else{
                deleteButton.setEnabled(false);
                if (already_deleted) {
                    deleteButton.setText("deleted");
                    if (archive.isInAWSInventory()) {
                        deleteButton.setToolTipText("still in AWS");
                    }
                }
                else {
                    deleteButton.setText("Delete");
                    deleteButton.setToolTipText("Not in AWS");
                }
            }
            add(deleteButton);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="start job">
            if (in_inventory && !already_deleted || isSearchData) {
                jobButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        new ArchiveJobDialog(ID,
                                ( vaultName == null )
                                ? archive.getVaultName() : vaultName,
                                 archive.getRegion());
                    }
                });
            } else {
                jobButton.setEnabled(false);
                if (already_deleted) {
                    jobButton.setToolTipText("Already deleted");
                } else {
                    jobButton.setToolTipText("Not in AWS inventory");
                }
            }
            add(jobButton);
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="download button or not">
            if (!downloadReadyArchives.containsKey(ID)) {
                add(new JLabel(""));
                continue;
            }            
            final JButton downloadButton = new JButton("download");
            add(downloadButton);
            DescribeJobResult result =
                    (DescribeJobResult) downloadReadyArchives.get(ID);
            archive.setJobId(result.getJobId());
            downloadButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    DownloadArchive da = new DownloadArchive(archive);
                    if (da.isCancelled()) {
                        return;
                    }
                    da.run();
                }
            });
            //</editor-fold>
            
        }
        init(rows, getColumns());
    }

    private class LaunchMetadataDialog extends SwingWorker<Void, Void> {
        private ArchiveMetadata am;
        
        LaunchMetadataDialog(final ArchiveMetadata _mar){
            am = _mar;
        }
        
        @Override
        public Void doInBackground() {
            new MSplash(am);
            return null;
        }
    }
    
}
