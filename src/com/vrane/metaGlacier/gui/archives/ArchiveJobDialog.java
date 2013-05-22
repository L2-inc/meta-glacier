/*
 * @(#)ArchiveJobDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.Main;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class ArchiveJobDialog extends JDialog {
    
    private final static Preferences P
            = Preferences.userNodeForPackage(ArchiveJobDialog.class);
    private final static Logger LGR
            = Main.getLogger(ArchiveJobDialog.class);
    private final static String SNS_TOPIC_KEY = "sns_topic_key";

    public ArchiveJobDialog(final String archiveId,final String vault,
            final String region){
        super(Main.frame, true);
        JPanel mainP = new JPanel(new GridLayout(0, 2, 5, 5));
        final JTextField jt = new JTextField(13);
        final JTextField snsJT = new JTextField(13);
        JButton jobButton = new JButton("create job");
       
        mainP.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        mainP.add(new JLabel("      Archive ID"));
        jt.setText(archiveId);
        jt.setEditable(false);
        mainP.add(jt);
        
        mainP.add(new JLabel("       SNS topic"));
        snsJT.setText(P.get(SNS_TOPIC_KEY + region, ""));
        mainP.add(snsJT);
        
        mainP.add(new JLabel());
        mainP.add(jobButton);
        jobButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                final String snsTopic = snsJT.getText();
                if (snsTopic == null || snsTopic.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "No sns topic specified");
                    return;
                }
                final Archive a = new Archive(vault, archiveId, region);
                final String jobId = a.createDownloadJob(snsTopic);
                ArchiveJobDialog.this.dispose();
                if (jobId.isEmpty()) {
                   JOptionPane.showMessageDialog(null,
                           "No job id returned; no such archive at AWS"); 
                   return;
                }
                LGR.log(Level.FINE, "job id is {0}", jobId);
                P.put(SNS_TOPIC_KEY + region, snsTopic);
            }
        });
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);   
    }
    
}
