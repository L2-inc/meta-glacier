/*
 * @(#)LocationOptionDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author kz
 */
class LocationOptionDialog extends JDialog {
    private final static int[] radii = {1, 5, 10, 20, 100};

    private boolean si_unit;
    private int selectedCCindex = -1;
    private int selectedRadiusIndex = -1;
    
    private final JRadioButton km = new JRadioButton("km");
    private final JRadioButton mile = new JRadioButton("mile");
    private final String[] ccs = Locale.getISOCountries();
    private final JComboBox countryCodes = new JComboBox();
    private final JComboBox radius = new JComboBox();
    private final ActionListener unitListener = new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent ae) {
            final String ac = ae.getActionCommand();
            
            si_unit = ac.equals("si");
        }
    };
    
    public LocationOptionDialog(final String selectedCC,
            final int selectedRadius,
            final boolean using_si_unit){
        super(Main.frame, true);
        final Locale defLoc = Locale.getDefault();
        final String defLan = defLoc.getLanguage();
        final SpringPanel sp = new SpringPanel();
        JLabel dummyLabel = sp.addLabel("country");        
        final ButtonGroup bg = new ButtonGroup();
        final JPanel lastPanel = new JPanel();
        final JButton okButton = new JButton("ok");
        final JButton cancelBut = new JButton("cancel");
        
        setLocationRelativeTo(Main.frame);
        
        //<editor-fold defaultstate="collapsed" desc="country code row">
        int i = 1;
        countryCodes.addItem(" -- no country --");
        for (final String cc: ccs) {
            if (cc.equals(selectedCC)) {
                selectedCCindex = i;
            }
            i++;
            countryCodes.addItem(new Locale(defLan, cc).getDisplayCountry());
        }
        countryCodes.setSelectedIndex(selectedCCindex);
        dummyLabel.setLabelFor(countryCodes);        
        sp.add(countryCodes);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="distance">
        i = 0;
        for (final int r: radii) {
            if (r == selectedRadius) {
                selectedRadiusIndex = i;
            }
            i++;
            radius.addItem(r);
        }
        if (selectedRadiusIndex == -1) {
            selectedRadiusIndex = 2;
        }
        radius.setSelectedIndex(selectedRadiusIndex);
        dummyLabel = sp.addLabel("distance");
        dummyLabel.setLabelFor(radius);
        sp.add(radius);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="unit">
        si_unit = using_si_unit;
        bg.add(mile);
        bg.add(km);
        km.setActionCommand("si");
        mile.setActionCommand("");
        km.addActionListener(unitListener);
        mile.addActionListener(unitListener);
        if (si_unit) {
            km.setSelected(true);
        } else {
            mile.setSelected(true);
        }
        final JPanel bgPanel = new JPanel();
        bgPanel.add(km);
        bgPanel.add(mile);
        dummyLabel = sp.addLabel("unit");
        dummyLabel.setLabelFor(bgPanel);
        sp.add(bgPanel);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="last row">

        cancelBut.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                LocationOptionDialog.this.dispose();
            }
        });
        okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                selectedCCindex = countryCodes.getSelectedIndex();
                selectedRadiusIndex = radius.getSelectedIndex();
                dispose();
            }
        });
        lastPanel.add(okButton);
        lastPanel.add(cancelBut);
        dummyLabel = sp.addLabel("");
        dummyLabel.setLabelFor(lastPanel);
        sp.add(lastPanel);
        sp.makeIt((short) 4, (byte) 2);
        add(sp);
        //</editor-fold>
        pack();
        setVisible(true);
    }
    
    public String getCountryCode(){
        return (selectedCCindex > 0) ? ccs[selectedCCindex - 1] : null;
    }
    
    public int getRadius(){
        return radii[selectedRadiusIndex];
    }
    
    public boolean use_si_unit(){
        return si_unit;
    }
}
