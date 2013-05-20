/*
 * @(#)ArchiveContentPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.gui.utilities.ByteTextField;
import com.vrane.metaGlacier.gui.utilities.ListPanelModel;
import com.vrane.metaGlacierSDK.BasicFileMetadata;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTextField;

class ArchiveContentPanel extends ListPanelModel{
    private ArrayList<BasicFileMetadata> completeList;
    
    ArchiveContentPanel withList(ArrayList<BasicFileMetadata> list){
        completeList = list;
        return this;
    }
    
    void init(){
        JTextField jt;
        short count = 0;
        byte rows = 1;
        
        for (final BasicFileMetadata ar: completeList) {
            count++;
            if (count < minimum || count > maximum) {
                continue;
            }
            rows++;
            //<editor-fold defaultstate="collapsed" desc="archive id label">
            add(new JLabel(count + ""));
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="file name">
            jt = new JTextField(ar.getName());
            jt.setEditable(false);
            add(jt);
           
            //</editor-fold>
                        
            //<editor-fold defaultstate="collapsed" desc="file size">
            jt = new ByteTextField(ar.getSize());
            jt.setEditable(false);
            add(jt);
            
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="Last Modified">
            jt = new JTextField(new Date(Long.parseLong(ar.getLastMod()))
                    .toString());
            jt.setEditable(false);
            add(jt);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="file type">
            jt = new JTextField(ar.getMimeType());
            jt.setEditable(false);
            add(jt);
            //</editor-fold>
            
        }
        init(rows, getColumns());
    }
}
