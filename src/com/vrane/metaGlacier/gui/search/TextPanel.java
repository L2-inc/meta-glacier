/*
 * @(#)TextPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

class TextPanel extends SpringPanel{
    private final JTextField left_jt = new JTextField(8);
    private final JTextField right_jt = new JTextField(8);
    
    public TextPanel(final String left_label, final String right_label){
        JLabel dummyLabel = addLabel(left_label);       
        
        dummyLabel.setLabelFor(left_jt);
        
        add(left_jt);
        
        add(new ClearLabel(left_jt));
        
        add(new JLabel("  ...  "));
        
        dummyLabel = addLabel(right_label);
        dummyLabel.setLabelFor(right_jt);
        add(right_jt);
        
        add(new ClearLabel(right_jt));

        makeIt((short) 1, (byte) 7);
    }
    
    public String get_left(){
        return left_jt.getText();
    }
    
    public String get_right(){
        return right_jt.getText();
    }
    
}
