/*
 * @(#)SpringPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.utilities;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import springutilities.SpringUtilities;

/**
 * Use this panel to use <code>SpringLayout</code>.
 *
 * @author K Z Win
 */
public class SpringPanel extends JPanel{
    private final static int INITIAL_XY = 6;
    private final static int INITIAL_XY_PAD = 6;

    /**
     *Sole constructor.
     */
    public SpringPanel(){
        super(new SpringLayout());
    }

    /**
     * Creates a <code>JLabel</code>, adds to this panel and returns it.
     *
     * @param labelText text to be shown in the label
     * @return <code>JLabel</code> object
     */
    public JLabel addLabel(String labelText){
        JLabel addedLabel = new JLabel(labelText, JLabel.LEADING);
        
        add(addedLabel);
        return addedLabel;
    }

    /**
     * Call this to finish constructing a spring panel.
     *
     * @param rows
     * @param columns
     */
    public void makeIt(final short rows, byte columns){
        SpringUtilities.makeCompactGrid(this, rows, columns, INITIAL_XY,
                INITIAL_XY, INITIAL_XY_PAD, INITIAL_XY_PAD);
    }
}
