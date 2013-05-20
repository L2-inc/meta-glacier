/*
 * @(#)FileProgressPanel.java  0.7 2013 May 15
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui.archives;

import com.vrane.metaGlacier.FileProgressReporter;
import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Utilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This is the panel  dedicated to displaying the progress of uploading task.
 * 
 * @author K Z Win
 */
public class FileProgressPanel extends JPanel implements FileProgressReporter{

    private final static Logger LGR
            = Main.getLogger(FileProgressPanel.class);
    
    private float most_recent_rate = (float) 0.;
    private final static byte MAX_FILENAME_LABEL_SIZE = 18;

    private final JProgressBar currentFileSizePb
            = new JProgressBar(SwingConstants.HORIZONTAL);
    private final JTextField rateText = new JTextField();
    private final JLabel currentFileLabel = new JLabel("Current file progress");
    private final JLabel TIME_TO_FINISH = new JLabel();
    private long currentFileSize = 0;
    private long last_position = 0;
    private long last_update = 0;

    public FileProgressPanel() {
        super(new BorderLayout());
        JPanel innerPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        add(innerPanel, BorderLayout.PAGE_START);
        
        innerPanel.add(new JLabel("Estimated time to finish:"));
        innerPanel.add(TIME_TO_FINISH);
        
        innerPanel.add(currentFileLabel);
        innerPanel.add(currentFileSizePb);
        innerPanel.add(new JLabel("Recent file transfer rate in kB/s"));
        innerPanel.add(rateText);
        rateText.setEditable(false);
        
        currentFileSizePb.setMaximum(100);         
        currentFileSizePb.setIndeterminate(true);        
        currentFileSizePb.setStringPainted(true);
    }
    
    @Override
    public void setFileSize(final long file_size){
        currentFileSize = file_size;
        last_position = 0;
        last_update = 0;
    }

    @Override
    public void setFilePosition(final long current_pos) {
         final boolean notValid = -1 == current_pos;
        final long now = System.currentTimeMillis();

        currentFileSizePb.setIndeterminate(notValid);
        if (notValid) {
            return;
        }
        final int percent = (int) (100 * (float) current_pos / currentFileSize);
        currentFileSizePb.setValue(percent);
        if (last_position > 0 && last_update > 0) {
            final float delta = current_pos - last_position;
            final float delta_t = now - last_update;
            if (delta_t > 0) {
                most_recent_rate = delta / delta_t * 1000 / HumanBytes.KILO;
                String[] rateInfo = Utilities.getRateInfo(most_recent_rate,
                            currentFileSize - current_pos);
                resetRate(rateInfo[0]);
                TIME_TO_FINISH.setText("  " + rateInfo[1]);
            }
        }
        last_update = now;
        last_position = current_pos;
        updateTotalSize(current_pos);
    }
    void resetRate(String t){
        if (t != null) {
            rateText.setText(t);
            return;
        }
        String s = rateText.getText();
        if (s != null) {
            rateText.setText(s);
        }
    }


    public void reInitCurrentFilePb(String fn, String size) {
        String label = fn;

        LGR.log(Level.INFO,
                "Processing {0} with size {1}", new Object[]{fn, size});
        if (label.length() > MAX_FILENAME_LABEL_SIZE) {
            label = fn.substring(0, MAX_FILENAME_LABEL_SIZE - 4) + "...";
            currentFileLabel.setToolTipText(fn);
        }
        TIME_TO_FINISH.setText("Calculating..");
        currentFileLabel.setText(label + " (" + size + ")");
    }

    @Override
    public void updateTotalSize(long current_position) {}
}
