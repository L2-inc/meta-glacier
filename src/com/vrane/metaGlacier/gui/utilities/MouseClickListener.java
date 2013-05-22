/*
 * @(#)MouseClickListener.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.utilities;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A mouse listener with all non-click events made no-ops.
 * 
 * @author me
 */
public abstract class MouseClickListener implements MouseListener{

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}
    
}
