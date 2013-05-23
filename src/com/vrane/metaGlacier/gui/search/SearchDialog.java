/*
 * @(#)SearchDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.desktop.MainFrame;
import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import com.vrane.metaGlacierSDK.Search;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * This is the UI dialog for search.
 * It creates a JDialog using other classes in the same package.
 * It calls the metadata-sdk "search" API and display the search result
 * appropriately.
 * 
 * @author K Z Win
 */
public class SearchDialog extends MainFrame{
    
    private final JTextField query = new JTextField(22);
    private final DatePanel uploadDates = new DatePanel();
    private final DatePanel contentDates = new DatePanel();
    private final DatePanel gpsDate = new DatePanel();
    private final DatePanel photoDates = new DatePanel();
    private final TextPanel cameraPanel = new TextPanel("make", "model");
    private final LongPanel sizePanel = new LongPanel();
    private final LongPanel resPanel = new LongPanel();
    private final JTextField locationBox = new JTextField(8);
    private final LocationPanel locationPanel = new LocationPanel(locationBox);

    /**
     * Constructs a window with all search fields.
     */
    public SearchDialog(){
        super("search", false);
        
        final SpringPanel sp = new SpringPanel();
        final JButton searchButton = new JButton("Search");
        final searchListener sl = new searchListener();
        
        sp.add(searchButton);
        sp.add(query);
        JLabel dummyLabel = sp.addLabel("Archive size ");
        add(sp);
        
        searchButton.addActionListener(sl);
        query.addActionListener(sl);
        
        dummyLabel.setLabelFor(sp);
        sp.add(sizePanel);
        
        dummyLabel = sp.addLabel("Archive upload date ");
        dummyLabel.setLabelFor(uploadDates);
        sp.add(uploadDates);
        
        dummyLabel = sp.addLabel("Archive content mod date ");
        dummyLabel.setLabelFor(contentDates);
        sp.add(contentDates);
        sp.add(new JSeparator(SwingConstants.HORIZONTAL));
        sp.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        dummyLabel = sp.addLabel("Photo resolution ");
        dummyLabel.setLabelFor(sp);
        sp.add(resPanel);
        
        dummyLabel = sp.addLabel("Photo device ");
        dummyLabel.setLabelFor(cameraPanel);
        sp.add(cameraPanel);
        
        dummyLabel = sp.addLabel("Photo date ");
        dummyLabel.setLabelFor(photoDates);
        sp.add(photoDates);
        
        sp.add(new JSeparator(SwingConstants.HORIZONTAL));
        sp.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        dummyLabel = sp.addLabel("GPS date ");
        dummyLabel.setLabelFor(gpsDate);
        sp.add(gpsDate);
        
        dummyLabel = sp.addLabel("location ");
        dummyLabel.setLabelFor(locationPanel);
        sp.add(locationPanel);
        locationBox.addActionListener(sl);
        
        sp.makeIt((short) 11, (byte) 2);
        pack();
        setResizable(false);
    }
    
    private class searchListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            Search s = new Search()
                    .withQuery(query.getText())
                    .withLocation(locationPanel.get_location())
                    .withRadius(locationPanel.get_radius())
                    .withMake(cameraPanel.get_left())
                    .withModel(cameraPanel.get_right())
                    .withMinRes(resPanel.get_min())
                    .withMaxRes(resPanel.get_max())
                    .withFileMinSize(sizePanel.get_min())
                    .withFileMaxSize(sizePanel.get_max())
                    .withGPSFromDate(gpsDate.after_epoch())
                    .withGPSToDate(gpsDate.before_epoch())
                    .withPhotoMinDate(photoDates.after_epoch())
                    .withPhotoMaxDate(photoDates.before_epoch())
                    .withUploadFromDate(uploadDates.after_epoch())
                    .withUploadToDate(uploadDates.before_epoch())
                    .withFileModFromDate(contentDates.after_epoch())
                    .withFileModToDate(contentDates.before_epoch());
            new LaunchArchiveManager(s).execute();
        }
    }
    
    private class LaunchArchiveManager extends SwingWorker<Void, Void> {
        private final Search se;
        
        LaunchArchiveManager(final Search _s){
            se =_s;
        }
        
        @Override
        public Void doInBackground() {
            new SearchSplash(se);
            return null;
        }
    }     
}