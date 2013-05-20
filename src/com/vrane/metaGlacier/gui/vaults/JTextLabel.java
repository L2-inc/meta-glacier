/*
 * @(#)JTextLabel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.vaults;

import javax.swing.JTextField;

class JTextLabel extends JTextField{
    
    JTextLabel(final String s){
        super(s);
        setEditable(false);
    }
    
}
