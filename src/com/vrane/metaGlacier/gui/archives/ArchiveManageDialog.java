/*
 * @(#)ArchiveManageDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacierSDK.MArchive;
import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ArchiveManageDialog extends JDialog{
    private final static String[] HEADER_COLUMNS = {"", "   Size", "", "", ""};
    private final static byte PAGE_SIZE = 20;
    private final static Logger LGR
            = Main.getLogger(ArchiveManageDialog.class);
    
    private JPanel listContainer;
    private long listSize;
    private int min = 1;
    private int max = 20;
    private ArchiveListPanel alp;

    public ArchiveManageDialog(final String vault_name,
            final ArrayList<Archive> Archives,
            final ArrayList<MArchive> archivesFromMetadata,
            final boolean isSearchResult,
            final String title){
        super(Main.frame, true);
        final JPanel headerPanel = new JPanel();
        final JPanel statPanel = new JPanel(new GridLayout(1, 3));
        JPanel jp = new JPanel();
        JTextField jt = new JTextField();
                
        listContainer = new JPanel(new BorderLayout());

        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

        alp = new ArchiveListPanel(vault_name)
                .withSearchFlag(isSearchResult)
                .withList(Archives);
        if (!archivesFromMetadata.isEmpty()) {
            LGR.log(Level.INFO, "number of archives {0}",
                    archivesFromMetadata.size());
            alp.setExtraList(archivesFromMetadata);
        }
        LGR.fine("creating archive job list");
        try {
            alp.makeArchiveJobList();
        } catch (Exception ex) {
            LGR.log(Level.SEVERE, null, ex);
        }
        LGR.fine("created archive job list");
        alp.setMax(max);
        alp.setMin(min);
        alp.setHeaders(HEADER_COLUMNS);
        alp.init();
        listContainer.add(alp, BorderLayout.CENTER);
        listSize = alp.mergedListSize();

        headerPanel.setLayout(new GridLayout(0, 1));
        
        if (listSize < 1) {
            String message = "No archives found.";
            if (!isSearchResult) {
                message += " AWS inventory out of date";
            }
            JOptionPane.showMessageDialog(null, message);
            return;
        }
        LGR.log(Level.INFO, "number of archives to display {0}", listSize);
        jt.setText(listSize + " archives");
        jt.setEnabled(false);
        statPanel.add(jt);
        final long numbersInAWS = (Archives == null) ? 0 : Archives.size();
        jt = new JTextField(numbersInAWS + " in AWS");
        jt.setEnabled(false);
        statPanel.add(jt);
        jt = new JTextField("Size: " + HumanBytes.convert(alp.getTotalSize()));
        statPanel.add(jt);
        jt.setEnabled(false);
        jt.setHorizontalAlignment(JTextField.RIGHT);
        headerPanel.add(statPanel);
        listContainer.add(headerPanel, BorderLayout.NORTH);
        
        if (listSize > PAGE_SIZE) {
            JPanel paginationPanel = new JPanel(new GridLayout(1, 2));
            final JButton prev = new JButton("previous");
            final JButton next = new JButton("next");
           
            headerPanel.add(paginationPanel);
            prev.setEnabled(false);
            paginationPanel.add(prev);
            prev.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!prev.isEnabled()) {
                        return;
                    }
                    prev.setEnabled(true);
                    min -= PAGE_SIZE;
                    int minToSet = min;
                    if ( min < 2 ) {
                        minToSet = 1;
                        prev.setEnabled(false);
                    }
                    max -= PAGE_SIZE;
                    if (max < listSize) {
                        next.setEnabled(true);
                    }
                    listContainer.remove(alp);
                    alp = new ArchiveListPanel(vault_name)
                            .withSearchFlag(isSearchResult)
                            .withList(Archives);
                    if (!archivesFromMetadata.isEmpty()) {
                        alp.setExtraList(archivesFromMetadata);
                    }
                    alp.setMax(max);
                    alp.setMin(minToSet);
                    alp.setHeaders(HEADER_COLUMNS);
                    alp.init(); 
                    // Ignore num rows since there is no need to do filling up
                    listContainer.add(alp, BorderLayout.CENTER);                
                    ArchiveManageDialog.this.pack();
                }
            });
            
            paginationPanel.add(next);
            next.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!next.isEnabled() || listSize < max) {
                        return;
                    }
                    max += PAGE_SIZE;
                    long maxToSet = max;
                    if (max >= listSize) {
                        maxToSet = listSize;
                        next.setEnabled(false); 
                    }
                    prev.setEnabled(true);
                    min += PAGE_SIZE;
                    listContainer.remove(alp);
                    alp = new ArchiveListPanel(vault_name)
                            .withSearchFlag(isSearchResult)
                            .withList(Archives);
                    if (!archivesFromMetadata.isEmpty()) {
                        alp.setExtraList(archivesFromMetadata);
                    }
                    alp.setMax((int)maxToSet);
                    alp.setMin(min);
                    alp.setHeaders(HEADER_COLUMNS);
                    alp.init();
                    listContainer.add(alp, BorderLayout.CENTER);
                    ArchiveManageDialog.this.pack();
                }
            });
        }
        JPanel VPanel = new JPanel();
        VPanel.add(GlacierFrame.makeLabelWithLength(title, (byte) 35));
        jp.add(VPanel);
        jp.add(listContainer);
        add(jp);
        pack();
        setLocationRelativeTo(Main.frame);
        setResizable(false);
        setModalityType(JDialog.ModalityType.DOCUMENT_MODAL);
    }
     
}
