/*
 * @(#)LocationPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

class LocationPanel extends SpringPanel{
    private final static double KM_IN_MILE = 0.621371;
    private final static Preferences P
            = Preferences.userNodeForPackage(LocationPanel.class);
    private final static String SI_UNIT_KEY = "SI";
    private final static String CC_KEY = "CC";
    private final static String RADIUS_KEY = "RADIUS";
    
    private String selectedCC = P.get(CC_KEY, null);
    private int selectedRadius = P.getInt(RADIUS_KEY, 10);
    private boolean si_unit = P.getBoolean(SI_UNIT_KEY, true);
    private final JTextField locationJT;
    private final JButton locationButton = new JButton("options");
    
    LocationPanel(final JTextField location_jt){
        locationJT = location_jt;
        add(locationJT);
        
        add(new ClearLabel(locationJT));
        
        add(new JLabel("  ...  "));
        
        add(locationButton);
        locationButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                LocationOptionDialog lod
                        = new LocationOptionDialog(selectedCC, selectedRadius,
                                                    si_unit);
                
                selectedCC = lod.getCountryCode();
                if (selectedCC == null) {
                    P.remove(CC_KEY);
                } else {
                    P.put(CC_KEY, selectedCC);
                }
                selectedRadius = lod.getRadius();
                P.putInt(RADIUS_KEY, selectedRadius);
                si_unit = lod.use_si_unit();
                P.putBoolean(SI_UNIT_KEY, si_unit);
            }
        });
        
        makeIt((short) 1, (byte) 4);
    }
        
    String get_location(){ 
        final String noCountry = locationJT.getText();
        
        if (noCountry == null || noCountry.isEmpty()) {
            return null;
        }
        return noCountry + (selectedCC == null ? "" : " " + selectedCC);
    }
    
    double get_radius(){
        return selectedRadius * 1000. * (si_unit ? 1. : KM_IN_MILE);
    }   
}
