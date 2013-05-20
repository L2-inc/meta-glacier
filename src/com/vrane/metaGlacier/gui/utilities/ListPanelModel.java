/*
 * @(#)ListPanelModel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.utilities;

import javax.swing.JLabel;

public abstract class ListPanelModel extends SpringPanel{
    
    protected int maximum = Integer.MAX_VALUE;
    protected int minimum = 1;
    private short columns;
    
    protected ListPanelModel(){}
    
    public void setMax(final int max){
        maximum = max;
    }
    
    public void setMin(final int min){
        minimum = min;
    }
    
    public void setHeaders(final String[] headers){
        for (final String h: headers) {
            add(new JLabel(h));
        }
        columns = (short) headers.length;
    }
    
    protected void init(final short rows, final short columns){
        makeIt(rows, (byte) columns);
    }
    
    protected short getColumns(){
        return columns;
    }    
}
