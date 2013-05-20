/*
 * @(#)DatePanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.search;

import com.vrane.datechooser.DateChooser;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

class DatePanel extends SpringPanel{
    private final static String DATE_FORMAT = "yyyy-MM-dd";

    private final JTextField after_date = new JTextField(8);
    private final JTextField before_date = new JTextField(8);
    
    public DatePanel(){
        JLabel dummyLabel = addLabel("after");        

        after_date.setEditable(false);
        before_date.setEditable(false);
        
        after_date.addMouseListener(new MouseClickListener(){

            @Override
            public void mouseClicked(MouseEvent me) {
                DateDialog dc = new DateDialog();
                String max_date = before_date.getName();
                
                dc.init();
                String min_date = dc.selectedEpoch();
                if (max_date != null) {
                    long max_epoch = Long.parseLong(max_date);
                    long min_epoch = Long.parseLong(min_date);
                    if (min_epoch >= max_epoch) {
                        JOptionPane.showMessageDialog(null,
                                "This date must be earlier than 'before' date");
                        after_date.setText(null);
                        after_date.setName(null);
                        return;
                    }
                }
                after_date.setText(dc.selectedDateString());
                after_date.setName(min_date);
            }
        });
        before_date.addMouseListener(new MouseClickListener(){

            @Override
            public void mouseClicked(MouseEvent me) {
                DateDialog dc = new DateDialog();
                String min_date = after_date.getName();
                
                dc.init();
                String max_date = dc.selectedEpoch();
                if (min_date != null) {
                    long max_epoch = Long.parseLong(max_date);
                    long min_epoch = Long.parseLong(min_date);
                    if (min_epoch >= max_epoch) {
                        JOptionPane.showMessageDialog(null,
                                "This date must be later than 'after' date");
                        before_date.setText(null);
                        before_date.setName(null);
                        return;
                    }
                }
                before_date.setText(dc.selectedDateString());
                before_date.setName(max_date);
            }
        });

        dummyLabel.setLabelFor(after_date);
        add(after_date);
        
        add(new ClearLabel(after_date));
        
        add(new JLabel("  ...  "));
        
        dummyLabel = addLabel("before");
        
        dummyLabel.setLabelFor(before_date);
        add(before_date);
        
        add(new ClearLabel(before_date));

        makeIt((short) 1, (byte) 7);
    }

    private class DateDialog extends JDialog{
        
        private DateChooser date_chooser;
        
        DateDialog(){
            super(Main.frame, true);
            date_chooser = new DateChooser(false, true);
            add(date_chooser);
            pack();
            setLocationRelativeTo(null);
        }

        void init(){
            date_chooser.withParent(this);
            setVisible(true);
        }

        String selectedDateString(){        
            return date_chooser.getSelectedDate(DATE_FORMAT);
        }

        String selectedEpoch(){
            return date_chooser.getSelectedDateEpoch(DATE_FORMAT).toString();
        }
    }
    
    public String after_epoch(){        
        return after_date.getName();
    }
    
    public String before_epoch(){
        return before_date.getName();
    }
    
}
