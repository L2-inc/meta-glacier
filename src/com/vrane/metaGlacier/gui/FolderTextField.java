/*
 * @(#)FolderTextField.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

class FolderTextField extends JTextField{
    
    private final static Preferences P
            = Preferences.userNodeForPackage(FolderTextField.class);
    
    FolderTextField(final String _key){
        super(P.get(_key, ""));
        setEditable(false);
        addMouseListener(new MouseClickListener(){

            @Override
            public void mouseClicked(MouseEvent me) {
                final JFileChooser fileChooser
                        = new JFileChooser(FolderTextField.this.getText());
                
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(FolderTextField.this) 
                        != JFileChooser.APPROVE_OPTION) {
                    FolderTextField.this.setText(null);
                    P.remove(_key);
                    return;
                }
                final File file = fileChooser.getSelectedFile();
                FolderTextField.this.setText(file.getAbsolutePath());
                P.put(_key, file.getAbsolutePath());                
            }
        });
    }
    
}
