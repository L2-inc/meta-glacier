/*
 * @(#)ChooseVaultDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Vault;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * This dialog window contains a drop-down combox box to choose or make a new
 * vault.
 * @author K Z Win
 */

class ChooseVaultDialog extends JDialog{
    
    private static String newVault;
    private final JComboBox toChoose = new JComboBox();
    
    static String getSelection(){
        return newVault;
    }
        
    ChooseVaultDialog(final String currentVault, final Set vaultNameSet){
        super(Main.frame, true);
        final String originalSelectedVault = currentVault;        
        JPanel P = new JPanel(new GridLayout(0, 1, 10, 10));
        JPanel mainP = new JPanel(new BorderLayout());
        
        P.setBorder(BorderFactory.createEmptyBorder(16, 15, 5, 15));
        mainP.add(P, BorderLayout.PAGE_START);
        P.add(new JLabel("Select or create vault"));
        toChoose.setToolTipText(
                "Select existing or type new vault here and press ENTER");
        toChoose.getEditor().addActionListener(new ActionListener(){
        // This is for creating a new vault
            @Override
            public void actionPerformed(ActionEvent ae) {
                newVault = (String) toChoose.getSelectedItem();
                if (vaultNameSet.contains(newVault)) {
                    MainPanel.setVaultSelection(newVault);
                    ChooseVaultDialog.this.dispose();
                }
                final int selectedButton = JOptionPane.showConfirmDialog(
                        Main.frame,
                        newVault,
                        "Create this vault?",
                        JOptionPane.YES_NO_OPTION);
                if (selectedButton != JOptionPane.YES_OPTION) {
                    ChooseVaultDialog.this.dispose();
                    newVault = originalSelectedVault;
                    return;
                }
                toChoose.setEnabled(false);
                final Vault v = new Vault(newVault);
                v.describe();
                if (v.exists()) {
                    JOptionPane.showMessageDialog(null,
                            "Vault name exists already");
                    return;
                }
                if (v.create()) {
                    MainPanel.setVaultSelection(newVault);
                    ChooseVaultDialog.this.dispose();
                    return;
                }
                String mes = "Failed to create.";
                if (v.vaultNameIsBad()) {
                    mes += " Bad name";
                }
                JOptionPane.showMessageDialog(null, mes);
            }
        });
        P.add(toChoose);
        add(mainP);
        pack();
        setLocationRelativeTo(Main.frame);
        String[] vaultNames = (String[]) vaultNameSet.toArray(new String[0]);
        Arrays.sort(vaultNames);
        for (final String vn: vaultNames) {
            toChoose.addItem(vn);
        }
        toChoose.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                String selection = (String) toChoose.getSelectedItem();
                
                if (vaultNameSet.contains(selection)) {
                    newVault = selection;
                    MainPanel.setVaultSelection(selection);
                    ChooseVaultDialog.this.dispose();                    
                }
            }
        });
        
        toChoose.setEditable(true);
        toChoose.setSelectedItem(currentVault);
        addWindowListener(new WindowAdapter(){
        
            @Override
            public void windowClosing(WindowEvent e) {
                newVault=null;            
            }
        });
        setVisible(true);
    }        
}