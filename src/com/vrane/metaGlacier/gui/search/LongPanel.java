/*
 * @(#)LongPanel.java  0.6 2013 May 5
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

class LongPanel extends SpringPanel{
    private final JTextField min_num = new JTextField(8);
    private final JTextField max_num = new JTextField(8);
    
    public LongPanel(){
        JLabel jl = addLabel("min");     
        
        jl.setLabelFor(min_num);
        add(min_num);
        
        add(new ClearLabel(min_num));
        
        add(new JLabel("  ...  "));
        
        jl = addLabel("max");
        
        jl.setLabelFor(max_num);
        add(max_num);
        
        add(new ClearLabel(max_num));

        makeIt((short) 1, (byte) 7);
    }
    
    public Long get_min(){
        return parseLong(min_num);
    }
    
    public Long get_max(){
        return parseLong(max_num);
    }
    
    private Long parseLong(final JTextField jt){
        final String a = jt.getText();
        
        if (a != null && !a.isEmpty()) {
            try {
                return Long.parseLong(a);
            } catch(Exception ex) {}
        }
        return null;
    }
    
}
