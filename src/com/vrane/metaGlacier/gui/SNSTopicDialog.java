/*
 * @(#)SNSTopicDialog.java  0.6 2013 May 7
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Main;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * This dialog window gather information for new metadata account.
 * @author K Z Win
 */
class SNSTopicDialog extends JDialog {
    private final static Logger LGR = Main.getLogger(SNSTopicDialog.class);

    SNSTopicDialog() {
        super(Main.frame, true);
        JPanel mainPanel = new JPanel(new GridLayout(4, 2));
        final JTextField topicNameJT = new JTextField(10);
        final JTextField emailJT = new JTextField(10);
        final JButton subscribeButton = new JButton("subscribe");
                
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        mainPanel.add(new JLabel("topic name"));
        mainPanel.add(topicNameJT);

        mainPanel.add(new JLabel("email"));
        mainPanel.add(emailJT);

        final String metadata_account_email = Main.frame.getMPCUser();
        if (metadata_account_email !=null ) {
            emailJT.setText(metadata_account_email);
        }
        
        mainPanel.add(new JLabel());
        mainPanel.add(subscribeButton);
        subscribeButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                final String email = emailJT.getText();
                final String name = topicNameJT.getText();
                boolean success = false;
                
                if (!EmailValidator.getInstance().isValid(email)) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid email address");
                    return;
                }
                try {
                    success = new SNSTopic().createTopic(name, email);
                } catch (Exception ex) {
                    LGR.log(Level.SEVERE, null, ex);
                }
                if(success){
                    JOptionPane.showMessageDialog(null,
                            "Please check your email to confirm");
                    dispose();
                    return;
                }
                JOptionPane.showMessageDialog(null, "Error subscribing");
            }
        });
        add(mainPanel);
        pack();
        setLocationRelativeTo(Main.frame);
        setVisible(true);
    }
}