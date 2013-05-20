/*
 * @(#)CSplash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.AllVaults;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.Splash;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This splash window is for selecting a vault name from the main Window.
 * 
 * @author K Z Win
 */
class CSplash extends Splash{
    private final static Logger LGR = Main.getLogger(CSplash.class);
    
    CSplash(final String current_vault){
        get_data(current_vault);
    }
        
    private void get_data(final String current){
        Set vs = null;
        try {
            vs = new AllVaults().listNames();
        } catch (Exception ex) {
            LGR.log(Level.SEVERE, null, ex);
        }
        
        dispose();
        if (vs == null) {
            JOptionPane.showMessageDialog(null,
                    "Error getting vaults. Check your connection or AWS keys");
            return;
        }
        new ChooseVaultDialog(current, vs);        
    }    
}
