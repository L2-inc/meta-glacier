/*
 * @(#)ClearLabel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author K Z Win
 */

class ClearLabel extends JLabel{
    
    ClearLabel(final JTextField jt){
        super("X");
        setForeground(Color.red);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new ClearButtonListener(jt));
    }
    
    private static class ClearButtonListener extends MouseClickListener {
        private final JTextField JTF;
        
        public ClearButtonListener(final JTextField jtf) {
            JTF = jtf;
        }

        @Override
        public void mouseClicked(MouseEvent ae) {
            JTF.setText(null);
            JTF.setName(null);
        }
    }
}
