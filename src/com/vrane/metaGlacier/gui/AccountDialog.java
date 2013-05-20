/*
 * @(#)AccountDialog.java  0.6 2013 May 5
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
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * This is a generic JDialog class for collecting account information from the user.
 * It is used both to collect AWS account and metadata account.
 * @author K Z Win
 */
class AccountDialog extends JDialog {

    private String userString = "";
    private String passString = "";
    private final Preferences pref;
    
    AccountDialog(final String userLabel, final String passLabel,
            final Preferences P, final String userKey, final String passKey,
            final String initUser, final String initPass){
        super(Main.frame, true);
        JPanel accountPanel = new JPanel(new GridLayout(4, 2));
        final JTextField userJT = new JTextField(10);
        final JPasswordField passJT = new JPasswordField(10);
        final JCheckBox saveCheckBox = new JCheckBox();
        final JButton deleteButton = new JButton("Delete saved credentials");
        final JButton setButton = new JButton("Set credentials");
        
        pref = P;
        accountPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        accountPanel.add(new JLabel(userLabel));
        accountPanel.add(userJT);

        accountPanel.add(new JLabel(passLabel));
        accountPanel.add(passJT);

        accountPanel.add(new JLabel("Check to save credentials"));
        accountPanel.add(saveCheckBox);

        accountPanel.add(deleteButton);
        
        userString = initUser;
        passString = initPass;
        
        userJT.setText(initUser);
        passJT.setText(initPass);

        deleteButton.addActionListener(new ActionListener(){
            

            @Override
            public void actionPerformed(ActionEvent ae) {
                P.remove(userKey);
                P.remove(passKey);
                userJT.setText(null);
                passJT.setText(null);
                userString = "";
                passString = "";
            }
        });

        accountPanel.add(setButton);
        setButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                passString = new String(passJT.getPassword());
                userString = userJT.getText();
                if (saveCheckBox.isSelected()) {
                    setPassword(passKey, passString);                    
                }
                P.put(userKey, userString);
                dispose();
            }
        });

        add(accountPanel);
        pack();
        setLocationRelativeTo(Main.frame);
        setVisible(true);
    }
    
    private void setPassword(final String _KEY, final String p){
        if (p.isEmpty()) {
            return;
        }
        pref.put(_KEY, GlacierFrame.encryptionObj.encrypt(p));
    }

    String getUser() {
        return userString;
    }
    
    String getPass(){
        return passString;
    }
}