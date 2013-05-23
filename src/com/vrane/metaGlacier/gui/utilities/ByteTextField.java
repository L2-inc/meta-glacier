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

/**
 * Represents a JTextField for showing file size.
 *
 * @author K. Z. Win
 */
public class ByteTextField extends JTextField{

    /**
     * Constructs a JTextField with properly formatted file size.
     * 
     * @param B size in bytes
     */
    public ByteTextField(final long B){
        super(6); // 6 is just right for windows; just a little too big for mac
        setText(HumanBytes.convert(B));
        setHorizontalAlignment(JTextField.RIGHT);
        setEditable(false);
    }
    
}
