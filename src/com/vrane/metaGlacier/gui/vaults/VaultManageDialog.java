/*
 * @(#)VaultManageDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.vaults;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Vault;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacierSDK.GlacierMetadata;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * Represents the window listing vaults in a region.
 *
 * @author me
 */
public class VaultManageDialog extends JDialog{
    
    private JPanel listContainer;
    private VaultListPanel listPanel;
    private short min = 1;
    private short max = 15;
    private byte pageSize = 15;
    private short listSize;

    /**
     * Sole constructor.
     *
     * @param list is a list of AWS objects containing vault list
     * @param deltaAWS time in ms taken to get this list from AWS
     * @param deltaAPI time in ms taken to get this list from metadata API
     * @param last_count a mapping of vault to the number of archives in it.
     */
    public VaultManageDialog(final List<DescribeVaultOutput> list,
            final long deltaAWS,
            final long deltaAPI, final HashMap last_count){
        super(Main.frame, true);
        
        JPanel createPanel =  new JPanel(new GridLayout(2, 1));
        final Border createPanelBorder
                = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);        
        final JTextField createJt = new JTextField();
        final JButton next = new JButton("next");
        final JPanel statPanel2 = new JPanel(new GridLayout(0, 2));
        
        listSize = (short) list.size();
                
        //<editor-fold defaultstate="collapsed" desc="create vault Panel">

        createPanel.setBorder(BorderFactory.createTitledBorder(
                createPanelBorder, "create vault"));
        createPanel.add(new JLabel(GlacierFrame.getAWSRegion()));
        createPanel.add(createJt);
        createJt.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                final String n = createJt.getText();
                Vault v = new Vault(n);
                
                v.describe();
                if (v.exists()) {
                    JOptionPane.showMessageDialog(null,
                            "Vault exists already");
                    return;
                }
                if (v.vaultNameIsBad()) {
                    JOptionPane.showMessageDialog(null,
                            "Bad vault name");
                    return;
                }
                if (v.create()) {
                    VaultManageDialog.this.dispose();
                    JOptionPane.showMessageDialog(null,
                            "Vault successfully created");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Failed to create");
                }
            }
        });
        add(createPanel,BorderLayout.NORTH);       
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="stats panel">
        if (list != null && !list.isEmpty()) {
            final JPanel statsPanel = new JPanel(new GridLayout(1, 3));
            statsPanel.setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.RAISED));
            long numArchives = 0;
            long totalSize = 0;
            
            for (final DescribeVaultOutput dvo: list) {
                numArchives += dvo.getNumberOfArchives();
                totalSize += dvo.getSizeInBytes();
            }
            JTextField jt = new JTextField(list.size() + " vaults");
            jt.setEditable(false);
            statsPanel.add(jt);
            jt = new JTextField(numArchives + " archives");
            statsPanel.add(jt);
            jt.setEditable(false);
            jt = new JTextField("Size: " + HumanBytes.convert(totalSize));
            statsPanel.add(jt);
            jt.setEditable(false);
            final JPanel emptyPanel = new JPanel();
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(4, 3, 4, 3));
            emptyPanel.add(statsPanel);
            add(emptyPanel, BorderLayout.CENTER);
        }
        //</editor-fold>
        
        listContainer = new JPanel(new BorderLayout());

        //<editor-fold defaultstate="collapsed" desc="list panel">
        listPanel = new VaultListPanel().withList(list);
        listPanel.setMin(min);
        listPanel.setMax(max);
        listPanel.init(last_count);
        
        if (listSize > pageSize) {
            JPanel pagesPanel = new JPanel(new GridLayout(1, 2));
            final JButton prev = new JButton("previous");
            
            listContainer.add(pagesPanel,BorderLayout.NORTH);
            prev.setEnabled(false);
            pagesPanel.add(prev);
            prev.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!prev.isEnabled()) {
                        return;
                    }
                    prev.setEnabled(true);
                    min -= pageSize;
                    short minToSet = min;
                    if ( min < 2 ) {
                        minToSet = 1;
                        prev.setEnabled(false);
                    }
                    max -= pageSize;
                    if (max < listSize) {
                        next.setEnabled(true);
                    }
                    listContainer.remove(listPanel);
                    listPanel = new VaultListPanel()
                            .withList(list);
                    listPanel.setMax(max);
                    listPanel.setMin(minToSet);
                    listPanel.init(last_count);
                    listContainer.add(listPanel, BorderLayout.CENTER);
                    VaultManageDialog.this.pack();
                }
            });
            
            pagesPanel.add(next);
            next.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!next.isEnabled() || listSize<max) {
                        return;
                    }
                    max += pageSize;
                    short maxToSet = max;
                    if (max >= listSize) {
                        maxToSet = listSize;
                        next.setEnabled(false); 
                    }
                    prev.setEnabled(true);
                    min += pageSize;
                    listContainer.remove(listPanel);
                    listPanel = new VaultListPanel()
                            .withList(list);
                    listPanel.setMax(maxToSet);
                    listPanel.setMin(min);

                    listPanel.init(last_count);
                    listContainer.add(listPanel, BorderLayout.CENTER);
                    VaultManageDialog.this.pack();
                }
            });
        }
        
        
        listContainer.add(listPanel, BorderLayout.CENTER);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="bottom stats panel">
        statPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextLabel dummy =new JTextLabel("AWS time/ms :" + deltaAWS);
        dummy.setToolTipText("time to get this list from AWS");
        statPanel2.add(dummy);
        statPanel2.add(new StatsLabel(
                "AWS calls :" + GlacierFrame.getAWSCalls(),
                GlacierFrame.getAWSapiLastReset()));
        if (deltaAPI > 0) {
            dummy = new JTextLabel("Metadata time/ms :" + deltaAPI);
            dummy.setToolTipText("time to get this list from metadata API");
            statPanel2.add(dummy);
            statPanel2.add(new StatsLabel("Metadata calls :" +
                    GlacierMetadata.getAPIcounter(),
                    GlacierFrame.getAPILastReset()));
        }
        listContainer.add(statPanel2, BorderLayout.SOUTH);
        //</editor-fold>
        
        add(listContainer, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(Main.frame);
        setResizable(false);
        setModalityType(JDialog.ModalityType.DOCUMENT_MODAL);
        setVisible(true);
    }
    
    private class StatsLabel extends JTextLabel{
        
        StatsLabel(final String s, final String tt){
            super(s);
            this.setToolTipText(tt);
        }
    }
}
