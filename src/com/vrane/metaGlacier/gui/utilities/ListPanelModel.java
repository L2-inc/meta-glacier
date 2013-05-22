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

    /**
     * Sets the maximum index.
     * 
     * @param max
     */
    public void setMax(final int max){
        maximum = max;
    }

    /**
     * Sets minimum index.
     * @param min
     */
    public void setMin(final int min){
        minimum = min;
    }

    /**
     * Set the header strings as an array.
     * 
     * @param headers
     */
    public void setHeaders(final String[] headers){
        for (final String h: headers) {
            add(new JLabel(h));
        }
        columns = (short) headers.length;
    }

    /**
     * Initializes this "spreadsheet" panel containing a list of things with
     * the number of rows and columns.
     * 
     * @param rows row count
     * @param columns column count
     */
    protected void init(final short rows, final short columns){
        makeIt(rows, (byte) columns);
    }

    /**
     * Returns the number of columns.
     *
     * @return column count.
     */
    protected short getColumns(){
        return columns;
    }    
}
