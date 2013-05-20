/*
 * @(#)Splash.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.utilities;

import java.awt.Cursor;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Use this model dialog as a superclass whenever you want user to wait for a 
 * long running network action.
 * @author K Z Win
 */
public abstract class Splash extends JDialog{
    
    private final JTextArea logg = new JTextArea(7, 50);
    
    /**
     * Use this method to send appropriate message to the user from your
     * subclass.
     * @param m 
     */
    public final void say(final String m){
        logg.insert(m + "\n", 0);
    }
    
    public Splash(){
        final JPanel jpa = new JPanel();
        final JProgressBar PB = new JProgressBar();

        say("\nPlease wait ...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setAlwaysOnTop(true);
        logg.setLineWrap(true);
        logg.setMargin(new Insets(5, 5, 5, 5));
        logg.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logg);
        logScrollPane.setAutoscrolls(true);
        jpa.setLayout(new BoxLayout(jpa, BoxLayout.Y_AXIS));
        PB.setIndeterminate(true);
        jpa.add(PB);
        jpa.add(logScrollPane);
        add(jpa);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
