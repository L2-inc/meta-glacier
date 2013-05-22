/*
 * @(#)VaultInventoryDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.vaults;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Vault;
import com.vrane.metaGlacier.gui.GlacierFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class VaultInventoryJobDialog extends JDialog {
    
    private final static Preferences P =
            Preferences.userNodeForPackage(VaultInventoryJobDialog.class);
    private final static String SNS_TOPIC_KEY = "sns_topic_key";
    private final static Logger LGR =
            Main.getLogger(VaultInventoryJobDialog.class);

    VaultInventoryJobDialog(final String name){
        super(Main.frame,true);
        JPanel mainP = new JPanel(new BorderLayout());
        JPanel holderPanel = new JPanel();
        final JTextField snsJT = new JTextField(20);
        final JButton jobButton = new JButton("create job");
        
        mainP.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        holderPanel.add(new JLabel("  Get vault inventory for '" + name
                + "'     "));
        holderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainP.add(holderPanel, BorderLayout.NORTH);
        
        
        mainP.add(new JLabel(" SNS topic  "),BorderLayout.WEST);
        snsJT.setText(P.get(SNS_TOPIC_KEY + GlacierFrame.getAWSRegion(), ""));
        mainP.add(snsJT, BorderLayout.CENTER);
        
        holderPanel = new JPanel();
        holderPanel.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        holderPanel.add(jobButton);
        mainP.add(holderPanel, BorderLayout.SOUTH);
        jobButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                String snsTopic = snsJT.getText();
                
                if (snsTopic == null || snsTopic.isEmpty()) {
                    LGR.warning("No sns topic specified");
                    return;
                }
                final String noWhiteSpace = snsTopic.trim();
                String jobId = new Vault(name)
                        .makeVaultInventoryJob(noWhiteSpace);
                if (jobId == null) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to create job.\nPlease check your input");
                    return;
                }
                P.put(SNS_TOPIC_KEY + GlacierFrame.getAWSRegion(), noWhiteSpace);
                VaultInventoryJobDialog.this.dispose();
            }
        });
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);   
    }
    
}
