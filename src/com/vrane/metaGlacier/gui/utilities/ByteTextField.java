/*
 * @(#)ByteTextField.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.utilities;

import com.vrane.metaGlacier.HumanBytes;
import javax.swing.JTextField;

public class ByteTextField extends JTextField{
    
    public ByteTextField(final long B){
        super(6); // 6 is just right for windows; just a little too big for mac
        setText(HumanBytes.convert(B));
        setHorizontalAlignment(JTextField.RIGHT);
        setEditable(false);
    }
    
}
