/*
 * @(#)UploadDialog.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.Archive;
import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Utilities;
import com.vrane.metaGlacier.gui.archives.FileProgressPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This is the window dedicated to displaying the progress of uploading task.
 * 
 * @author K Z Win
 */
public class UploadDialog extends JFrame {

    private final static byte MAX_NUM_OF_RATES_STORED = 10;
    private final static Logger LGR
            = Main.getLogger(UploadDialog.class);
    private final static Preferences P
            = Preferences.userNodeForPackage(UploadDialog.class);
    private final static String STORED_RATES = "stored rates";
    
    private long finalTotalSize;
    private int files_to_upload;
    private UploadThread work;
    private float most_recent_rate = (float) 0.;
    
    private final JProgressBar fileCountPb
            = new JProgressBar(SwingConstants.HORIZONTAL);
    private final JProgressBar totalSizePb
            = new JProgressBar(SwingConstants.HORIZONTAL);
    private final JTextField rateText = new JTextField();
    private final static String MEAN_UPLOAD_RATE = "mean upload rate";
    private final static String MAX_UPLOAD_RATE = "max upload rate";
    private final static String MIN_UPLOAD_RATE = "min upload rate";
    private final static JLabel TIME_TO_FINISH = new JLabel();
    private final FileUploadProgressPanel fupp = new FileUploadProgressPanel();
    private long currentTotalSize = 0;
    
    UploadDialog(final UploadThread upload_thread, final String title) {
        JPanel innerPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JPanel outerPanel = new JPanel(new BorderLayout());

        setTitle(title);
        work = upload_thread;
        fileCountPb.setSize(300, 44);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        outerPanel.add(innerPanel, BorderLayout.PAGE_START);
        innerPanel.add(new JLabel("File count progress"));
        innerPanel.add(fileCountPb);
        
        innerPanel.add(new JLabel("Total size"));
        innerPanel.add(totalSizePb);
        
        innerPanel.add(new JLabel("Estimated time to finish all:"));
        innerPanel.add(TIME_TO_FINISH);
        
        innerPanel.add(new JLabel("Recent archive upload rate (kB/s)"));
        innerPanel.add(rateText);
        rateText.setEditable(false);
        
        add(outerPanel);

        totalSizePb.setIndeterminate(false);
        totalSizePb.setMaximum(100);
        totalSizePb.setStringPainted(true);
        fileCountPb.setIndeterminate(true);
        fileCountPb.setStringPainted(true);
        outerPanel.add(fupp);
        pack();
        setResizable(false);
        setLocationRelativeTo(Main.frame);
        addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent we) {
                LGR.info("cancelling job");
                if (work.isAlive()) {
                    work.interrupt();
                    try {
                        work.join();
                    } catch (InterruptedException ex) {
                        LGR.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }
    
    void resetRate(String t){
        if (t != null) {
            rateText.setText(t);
            return;
        }
        String s = rateText.getText();
        if (s != null) {
            rateText.setText(s);
            storeRate(s);
            most_recent_rate = Float.parseFloat(s);
        }
    }
    
    static float maxUploadRate(){
        return P.getFloat(MAX_UPLOAD_RATE, (float) 0.0);
    }
    
    static float minUploadRate(){
        return P.getFloat(MIN_UPLOAD_RATE, (float) 0.0);
    }
    
    static float meanUploadRate(){
        return P.getFloat(MEAN_UPLOAD_RATE, (float) 0.0);
    }
    
    private void storeRate(final String rate_string){
        final String stored_rate_string = P.get(STORED_RATES, "");
        String[] stored_rate_strings
                = new String[MAX_NUM_OF_RATES_STORED];
        String[] save_stored_rate_strings;
        final StringBuffer sb = new StringBuffer();
        float total_rate = 0;
        float max_rate = 0;
        float min_rate = Float.MAX_VALUE;
        float mean_rate = 0;
        
        if (stored_rate_string.equals("")) {
            P.put(STORED_RATES, rate_string);
            return;
        } else {
            save_stored_rate_strings = stored_rate_string.split(",");
            if (save_stored_rate_strings.length < MAX_NUM_OF_RATES_STORED) {
                stored_rate_strings
                        = new String[1 + save_stored_rate_strings.length];
                System.arraycopy(save_stored_rate_strings, 0,
                        stored_rate_strings, 0,
                        save_stored_rate_strings.length);
                stored_rate_strings[save_stored_rate_strings.length]
                        = rate_string;
            } else {
                LGR.log(Level.FINE,
                        "copying {0} rates from disk",
                        (MAX_NUM_OF_RATES_STORED -1 ));
                for (byte i = 1; i < MAX_NUM_OF_RATES_STORED; i++) {
                    stored_rate_strings[i - 1] = save_stored_rate_strings[i];
                }
                stored_rate_strings[MAX_NUM_OF_RATES_STORED - 1] = rate_string;
            }
        }
        byte i = 0;
        for (String s: stored_rate_strings) {
            LGR.log(Level.FINE, "saving rate {0}", s);
            sb.append(s);
            float r = Float.parseFloat(s);
            total_rate += r;
            if (r > max_rate) {
                max_rate = r;
            }
            if (r < min_rate) {
                min_rate = r;
            }            
            i++;
            if (i == stored_rate_strings.length){
                break;
            }
            sb.append(",");
        }
        mean_rate = total_rate / stored_rate_strings.length;
        P.putFloat(MEAN_UPLOAD_RATE, mean_rate);
        P.putFloat(MAX_UPLOAD_RATE, max_rate);
        P.putFloat(MIN_UPLOAD_RATE, min_rate);
        P.put(STORED_RATES, sb.toString());
    }
    
    void updateFileCount(final int currentCount) {
        if (currentCount == 1) {
            fileCountPb.setIndeterminate(false);
        }
        fileCountPb.setValue(currentCount);
        fileCountPb.setString(currentCount + "/" + files_to_upload);
    }  
    
    public void setCurrentTotalSize(long current_total_size) {
        currentTotalSize = current_total_size;
    }

    void reInitCurrentFilePb(String fn, String size) {
        fupp.reInitCurrentFilePb(fn, size);
    }
            
    void prepareNewWindow(final int count, final long size){
        most_recent_rate = meanUploadRate();
        fileCountPb.setMaximum(count);
        fileCountPb.setString("0/" + count);
        finalTotalSize = size;
        files_to_upload = count;
    }

    void setCurrentArchive(Archive a) {
        a.withProgressReporter(fupp);
        fupp.setFileSize(a.getSize());
    }

    private class FileUploadProgressPanel extends FileProgressPanel {

        @Override
        public void updateTotalSize(long current_position) {
            final long _current_total_size = current_position + currentTotalSize;
            int currentTotalSizePercent
                    = (int) ((float) _current_total_size * 100. /
                            (float) finalTotalSize);

            if (most_recent_rate > 0.) {
                String[] rate_info =
                        Utilities.getRateInfo(most_recent_rate,
                        finalTotalSize - _current_total_size);
                TIME_TO_FINISH.setText("  " + rate_info[1]);
            } else {
                TIME_TO_FINISH.setText("Collecting data...");
            }
            totalSizePb.setValue(currentTotalSizePercent);
            totalSizePb.setString(currentTotalSizePercent + "% of "
                    + HumanBytes.convert(finalTotalSize));
        }
    }
}
