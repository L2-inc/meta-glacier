/*
 * @(#)MetaDataSignUpDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacierSDK.SDKException;
import com.vrane.metaGlacierSDK.SignUp;
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
class MetaDataSignUpDialog extends JDialog {
    private final static Logger LGR
            = Main.getLogger(MetaDataSignUpDialog.class);

    MetaDataSignUpDialog() {
        super(Main.frame, true);
        JPanel signUpPanel = new JPanel(new GridLayout(4, 2));
        final JTextField nameJT = new JTextField(10);
        final JTextField emailJT = new JTextField(10);
        final JTextField emailJT1 = new JTextField(10);
        final JButton signUpButton = new JButton("Sign up");
                
        signUpPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        signUpPanel.add(new JLabel("name (optional)"));
        signUpPanel.add(nameJT);

        signUpPanel.add(new JLabel("email"));
        signUpPanel.add(emailJT);

        signUpPanel.add(new JLabel("email again"));
        signUpPanel.add(emailJT1);

        signUpPanel.add(new JLabel());

        signUpPanel.add(signUpButton);
        signUpButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                final String email = emailJT.getText();
                final String email1 = emailJT1.getText();
                final String name = nameJT.getText();
                boolean success = false;
                
                if (!email.equals(email1)) {
                    JOptionPane.showMessageDialog(null,
                            "Email addresses do not match");
                    return;
                }
                if (!EmailValidator.getInstance().isValid(email)) {
                    JOptionPane.showMessageDialog(null,
                            "Invalid email address");
                    return;
                }
                try {
                    success = new SignUp().signup(email, name);
                } catch (SDKException ex) {
                    LGR.log(Level.SEVERE, null, ex);
                }
                if(success){
                    JOptionPane.showMessageDialog(null,
                            "Please check your email to confirm");
                    dispose();
                    return;
                }
                JOptionPane.showMessageDialog(null,
                        "Error signing up");
            }
        });
        add(signUpPanel);
        pack();
        setLocationRelativeTo(Main.frame);
        setVisible(true);
    }
}
