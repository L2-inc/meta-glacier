/*
 * @(#)ArchiveMetadataDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacierSDK.APIException;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.ArchiveMetadata;
import com.vrane.metaGlacierSDK.BasicFileMetadata;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class ArchiveMetadataDialog extends JDialog{

    private final static String[] HEADER_COLUMNS
            = {"", "Name", "Size", "Last Modified", "Type"};
    private final static Logger LGR
            = Main.getLogger(ArchiveMetadataDialog.class);   
    
    private int listSize;
    private byte pageSize = 10;
    private int min = 1;
    private int max = 10;
    private ArchiveContentPanel acp;
    private JPanel headerPanel = new JPanel(new BorderLayout());
    private JPanel subHeaderPanel = new JPanel(new GridLayout(0, 2));
    private final JButton next = new JButton("next");
    
    ArchiveMetadataDialog(final ArchiveMetadata amd) {
        final ArrayList<BasicFileMetadata> metadataContents = amd.getContents();
        final JPanel listContainer = new JPanel(new BorderLayout());
        final JTextField awsID = new JTextField(15);
        final JPanel awsIDPanel = new JPanel();
        final JPanel topSubHeaderPanel = new JPanel(new GridLayout(1, 2));
        final JButton delMeta = new JButton("delete metadata");
        final JPanel delMetaPanel = new JPanel();
        
        listSize = metadataContents.size();
        if (listSize > 0) {
            if (listSize > pageSize) {
                JPanel paginationPanel = new JPanel(new GridLayout(1, 2));
                listContainer.add(paginationPanel, BorderLayout.NORTH);
                final JButton prev = new JButton("previous");
                prev.setEnabled(false);
                paginationPanel.add(prev);
                prev.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (!prev.isEnabled()) {
                            return;
                        }
                        prev.setEnabled(true);
                        min -= pageSize;
                        int minToSet = min;
                        if ( min < 2 ) {
                            minToSet = 1;
                            prev.setEnabled(false);
                        }
                        max -= pageSize;
                        if (max < listSize) {
                            next.setEnabled(true);
                        }
                        listContainer.remove(acp);
                        acp = new ArchiveContentPanel()
                                .withList(metadataContents);
                        acp.setMin(minToSet);
                        acp.setMax(max);
                        acp.setHeaders(HEADER_COLUMNS);
                        acp.init();
                        listContainer.add(acp, BorderLayout.CENTER);
                        ArchiveMetadataDialog.this.pack();
                    }
                });
                paginationPanel.add(next);
                next.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (!next.isEnabled()) {
                            return;
                        }
                        if (listSize < max) {
                            return;
                        }
                        max += pageSize;
                        int maxToSet = max;
                        if (max >= listSize) {
                            maxToSet = listSize;
                            next.setEnabled(false); 
                        }
                        prev.setEnabled(true);
                        min += pageSize;
                        listContainer.remove(acp);
                        acp = new ArchiveContentPanel()
                                .withList(metadataContents);
                        acp.setMin(min);
                        acp.setMax(maxToSet);
                        acp.setHeaders(HEADER_COLUMNS);
                        acp.init();
                        listContainer.add(acp, BorderLayout.CENTER);
                        ArchiveMetadataDialog.this.pack();
                    }
                });
            }
            acp = new ArchiveContentPanel().withList(metadataContents);
            acp.setMin(min);
            acp.setMax(max);
            acp.setHeaders(HEADER_COLUMNS);
            acp.init();
            listContainer.add(acp, BorderLayout.CENTER);
        }        

        awsIDPanel.setMaximumSize(new Dimension(20, 10));
        awsIDPanel.add(awsID);
        awsID.setText(amd.getArchiveId());
        topSubHeaderPanel.add(awsIDPanel);
        delMetaPanel.setMaximumSize(new Dimension(20, 10));
        delMetaPanel.add(delMeta);
        delMeta.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                int selection = JOptionPane.showConfirmDialog(null,
                        "This will not delete any thing from AWS.\nProceed?",
                        "What does this button do?",
                        JOptionPane.YES_NO_OPTION);
                if (JOptionPane.YES_OPTION != selection) {
                    return;
                }
                boolean success = false;
                try {
                    success = amd.rid();
                } catch (SDKException | APIException ex) {
                    LGR.log(Level.SEVERE, null, ex);
                }
                if (success) {
                    ArchiveMetadataDialog.this.dispose();
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Failed to delete metadata");
                }
            }
        });
        topSubHeaderPanel.add(delMetaPanel);
        headerPanel.add(topSubHeaderPanel, BorderLayout.NORTH);
        final JTextField clientArchiveIDLabel
                = new JTextField("" + amd.getClientArchiveId());
        headerPanel.add(clientArchiveIDLabel, BorderLayout.WEST);
        headerPanel.add(
                GlacierFrame.makeLabelWithLength(amd.getFileName(), (byte) 30),
                BorderLayout.EAST
                );

        JPanel jp= new JPanel();
        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
        
        final JTextField fileCountLabel = new JTextField(amd.getFileCount());
        final JTextField userLabel = new JTextField(amd.getCreateUser());
        final JTextField computerLabel = new JTextField(amd.getComputerId());
        
        subHeaderPanel.add(new JLabel("Count"));
        subHeaderPanel.add(fileCountLabel);
        subHeaderPanel.add(new JLabel("Upload user"));
        subHeaderPanel.add(userLabel);
        subHeaderPanel.add(new JLabel("Computer id"));
        subHeaderPanel.add(computerLabel);

        JTextField[] allLabels 
                = {computerLabel, awsID, clientArchiveIDLabel, userLabel, 
                    fileCountLabel};
        for (final JTextField _jt: allLabels) {
            _jt.setEditable(false);
        }
        headerPanel.add(subHeaderPanel, BorderLayout.SOUTH);
        jp.add(headerPanel);
        jp.add(listContainer);

        add(jp);
        setLocationRelativeTo(null);        
        setResizable(false);
        pack();
        setVisible(true);
    }
}
