/*
 * @(#)VaultListPanel.java  0.6 2013 May 5
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
import com.vrane.metaGlacier.gui.utilities.ByteTextField;
import com.vrane.metaGlacier.gui.utilities.ListPanelModel;
import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

class VaultListPanel extends ListPanelModel{
    private final static Logger LGR = Main.getLogger(VaultListPanel.class);
    
    private List<DescribeVaultOutput> list;
    
    VaultListPanel withList(final List<DescribeVaultOutput> _list){
        list = _list;
        return this;
    }
    
    void init(final HashMap<String, String> lastcount){
        JTextField jt;
        short count = 0;
        byte rows = 1;
        
        if (list.size() > 0) {
            add(new JLabel());      
            add(new HeaderJT("Archives"));
            add(new HeaderJT("Last count"));
            add(new HeaderJT("Size"));
            add(new JLabel());
        }
        for (final DescribeVaultOutput dvo: list) {
            count++;
            if (count < minimum || count > maximum) {
                continue;
            }
            rows++;
            final String vault_name = dvo.getVaultName();
            final JLabel la = GlacierFrame.makeLabel(" " + vault_name + " ");
            add(la);
            String buttonString;
            long numArchives = dvo.getNumberOfArchives();
            if (numArchives > 0) {
                buttonString = "Inventory";
                la.setBorder(BorderFactory.createRaisedBevelBorder());
                la.addMouseListener(new MouseClickListener(){

                    @Override
                    public void mouseClicked(MouseEvent me) {
                        new LaunchArchiveManager(vault_name).execute();
                    }
                });
            } else {
                buttonString = "Delete";
            }
            jt = new JTextField(5);
            jt.setText(numArchives + "");
            jt.setHorizontalAlignment(JTextField.RIGHT);
            jt.setEditable(false);
            add(jt);
            jt = new JTextField(5);
            String last_count_text = null;
            last_count_text = (String) (
                    lastcount.containsKey(vault_name) 
                    ? lastcount.get(vault_name) 
                    : "" + numArchives );
            jt.setText(last_count_text);

            jt.setHorizontalAlignment(JTextField.RIGHT);
            jt.setEditable(false);
            add(jt);            
            jt = new ByteTextField(dvo.getSizeInBytes());
            add(jt);
            final JButton vaultButton = new JButton(buttonString);
            vaultButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (!vaultButton.isEnabled()) {
                        return;
                    }
                    vaultButton.setCursor(
                            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    vaultButton.validate();
                    if (vaultButton.getText().equals("Delete")) {
                        vaultButton.setEnabled(false);
                        try {
                            if (new Vault(vault_name).delete()) {
                                vaultButton.setText("deleted");
                            }
                            else {
                                vaultButton.setEnabled(true);
                            }
                        } catch(Exception e) {
                            LGR.log(Level.SEVERE, null, e);
                            JOptionPane.showMessageDialog(null,
                                    "Failed to delete vault. An archive added recently?");
                            vaultButton.setEnabled(true);
                        }
                    } else {
                        new VaultInventoryJobDialog(vault_name);
                    }
                    vaultButton.setCursor(Cursor.getDefaultCursor());
                }
            });
            add(vaultButton);
        }
        init(rows, (short) 5);
    }

    private class LaunchArchiveManager extends SwingWorker<Void, Void> {
        private final String VaultName;
        
        LaunchArchiveManager(final String _v){
            VaultName =_v;
        }
        
        @Override
        public Void doInBackground() {
            new ASplash(VaultName);
            return null;
        }
    }    
    
    private class HeaderJT extends JTextField{
        
        HeaderJT(final String t){
            super(t);
            setHorizontalAlignment(JTextField.RIGHT);
            setEnabled(false);
        }
    }
}
