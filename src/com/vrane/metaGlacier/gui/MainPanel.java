/*
 * @(#)MainPanel.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.HumanBytes;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.Utilities;
import com.vrane.metaGlacier.Vault;
import com.vrane.metaGlacier.gui.utilities.MouseClickListener;
import com.vrane.metaGlacier.gui.utilities.SpringPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * This is the panel that the user sees on starting the application.
 * @author K Z Win
 */
class MainPanel extends SpringPanel{    
    private static JTextField descriptionTextField;
    
    private final static Preferences P
            = Preferences.userNodeForPackage(MainPanel.class);
    private final static Logger LGR = Main.getLogger(MainPanel.class);
    private final static String VAULTNAME_KEY = "vault name";
    private final static String LAST_DIR = "last dir";
    private final static String ZIP_SIZE_KEY = "zip size in MB";
    private final static String MOVEFOLDER_KEY = "movefolder";
    private final static JTextField VAULT_INPUT = new JTextField(15);        
    private final static JTextField ZIP_SIZE_INPUT = new JTextField();
    private final static JButton UPLOAD_BUTTON = new JButton("upload");
    private ArrayList<File> files_to_upload;

    MainPanel() {
        String zipSizeText = String.valueOf(P.getInt(ZIP_SIZE_KEY, 0));
        final JTextField moveFolder = new FolderTextField(MOVEFOLDER_KEY);
        final JTextField sleepValue = new JTextField();
        
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
     
        //<editor-fold defaultstate="collapsed" desc="vault name">        
        JLabel label = addLabel("Vault Name");
        label.setLabelFor(VAULT_INPUT);
        add(VAULT_INPUT);
        VAULT_INPUT.setToolTipText("Click here to set the vault name");
        VAULT_INPUT.setEditable(false);
        VAULT_INPUT.addMouseListener(new MouseClickListener(){

            @Override
            public void mouseClicked(MouseEvent me) {
                new LaunchVaultChooser().execute();
            }
        });
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="description">        
        label = addLabel("Description");
        descriptionTextField = new JTextField();
        label.setLabelFor(descriptionTextField);
        add(descriptionTextField);
        descriptionTextField.setToolTipText("Type description here");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="zip size">        
        label = addLabel("zip size in MB");
        label.setLabelFor(ZIP_SIZE_INPUT);
        add(ZIP_SIZE_INPUT);
        ZIP_SIZE_INPUT.setToolTipText("Type zip size here.");
        if (!zipSizeText.equals("0")) {
            ZIP_SIZE_INPUT.setText(zipSizeText);
        }
        ZIP_SIZE_INPUT.addMouseListener(new MouseListener(){
            
            @Override
            public void mouseEntered(MouseEvent me) {
                final String granularity
                        = HumanBytes.convert(Main.frame.getGranularity()); 
                String ttText = "Type zip size here. ";
                
                if (!granularity.equals("0.0 B")) {
                    ttText += " Upload size is " + granularity;
                }
                ZIP_SIZE_INPUT.setToolTipText(ttText);
            }
            
            @Override
            public void mouseClicked(MouseEvent me) {}

            @Override
            public void mousePressed(MouseEvent me) {}

            @Override
            public void mouseReleased(MouseEvent me) {}

            @Override
            public void mouseExited(MouseEvent me) {}
        });
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="move to folder">
        
        label = addLabel("folder to move to after upload");
        label.setLabelFor(moveFolder);
        add(moveFolder);
        moveFolder.setToolTipText(
                "Click to set a folder to move uploaded files; cancel folder dialog to clear this");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="choose files to upload part 1">        
        label = addLabel("choose files");
        final JTextField filesToUpload = new JTextField();
        filesToUpload.setEditable(false);
        filesToUpload.setText(P.get(LAST_DIR, ""));
        label.setLabelFor(filesToUpload);
        
        filesToUpload.addMouseListener(new MouseClickListener(){
            
            @Override
            public void mouseClicked(MouseEvent me) {
                String dir = P.get(LAST_DIR, "");
                JFileChooser fc = new JFileChooser(dir);
                final float min_rate = UploadDialog.minUploadRate();
                final float mean_rate = UploadDialog.meanUploadRate();
                final float max_rate = UploadDialog.maxUploadRate();
                final ArrayList<File> file_list = new ArrayList<>();

                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (fc.showOpenDialog(MainPanel.this)
                        != JFileChooser.APPROVE_OPTION ){
                    return;
                }
                
                File[] files = fc.getSelectedFiles();
                Integer numberOfFiles = files.length;
                dir = files[0].getParent();
             
                P.put(LAST_DIR, dir);
                filesToUpload.setText(dir);
                int count = 0;
                long totalSize = 0L;
                for (int i = 0; i < numberOfFiles; i++) {
                    ArrayList<String> paths=new ArrayList<>();
                    if (files[i].isDirectory()) {
                        paths = getFilesInDir(files[i].listFiles());
                    } else {
                        paths.add(files[i].getAbsolutePath());
                    }
                    for (String p: paths) {
                        count++;
                        LGR.finer(p);
                        File thisFile = new File(p);
                        file_list.add(thisFile);
                        totalSize += thisFile.length();
                    }
                }
                if (count < 1) {
                    return;
                }
                files_to_upload = file_list;
                LGR.log(Level.INFO, "Number of files {0}", count);
                LGR.log(Level.INFO, "Total file size {0}",
                        HumanBytes.convert(totalSize));
                if (mean_rate > 0.) {
                    LGR.log(Level.INFO,
                            "mean upload rate is {0} kB/s and expected to take {1}",
                            Utilities.getRateInfo(mean_rate, totalSize) );
                }
                if (max_rate > 0.) {
                    LGR.log(Level.INFO,
                            "max upload rate is {0} kB/s and expected to take {1}",
                            Utilities.getRateInfo(max_rate, totalSize));
                }
                if (min_rate > 0.) {
                    LGR.log(Level.INFO,
                            "min upload rate is {0} kB/s and expected to take {1}",
                            Utilities.getRateInfo(min_rate, totalSize));
                }
                String zipSizeString = ZIP_SIZE_INPUT.getText();
                UPLOAD_BUTTON.setEnabled(true);
                LGR.fine("enabling upload button");
                if (zipSizeString == null) {
                    return;
                }
                int zipSize;
                try {
                    zipSize = Integer.parseInt(zipSizeString);
                } catch (NumberFormatException ex) {
                    //This will be interpreted as user not wanting to zip
                    return;
                }
                final String zipName = "zip";
                final ZipArchives ar
                        = new ZipArchives(file_list,
                            zipSize * HumanBytes.MEGA, zipName);
                final HashMap<String,ArrayList<String>> zipList = ar.zipList;
                LGR.log(Level.INFO, "Total number of zip files {0}", zipList.size());
                for (Map.Entry<String,ArrayList<String>> e: zipList.entrySet()) {
                    LGR.log(Level.FINE, "zip name {0}", e.getKey());
                    LGR.fine("file list");
                    long currentZipSize = 0L;
                    for (String f: e.getValue()) {
                        LGR.log(Level.FINER, "\t{0}", f);
                        currentZipSize += new File(f).length();
                    }
                    LGR.log(Level.FINE, "zip size {0} number of files {1}", 
                            new Object[]{HumanBytes.convert(currentZipSize),
                                e.getValue().toArray().length});
                }                
            } 
        });
        add(filesToUpload);
        filesToUpload.setToolTipText("Click here to select your files");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="sleep minute input">        
        label = addLabel("sleep/minute");
        label.setLabelFor(sleepValue);
        sleepValue.setToolTipText(
                "Number of mintutes to wait before starting upload.");
        add(sleepValue);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="setting up upload button">        
        UPLOAD_BUTTON.setEnabled(false);
        label = addLabel("");
        label.setLabelFor(UPLOAD_BUTTON);
        add(UPLOAD_BUTTON);
        UPLOAD_BUTTON.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                String sleepString = sleepValue.getText();
               
                if (!UPLOAD_BUTTON.isEnabled()) {
                    return;
                }
                if (sleepString.equals("")) {
                    sleepString = "0";
                }
                int sleepMinute;
                try{
                    sleepMinute = Integer.parseInt(sleepString);
                } catch (NumberFormatException ex){
                    sleepMinute = 0;
                }
                              
                //<editor-fold defaultstate="collapsed" desc="get credentials">
                
                final String accessKeyString = Main.frame.getAWSAccessKeyId();
                if (accessKeyString.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Access key required.  Set it in the menu");
                    return;
                }
                
                String secretKeyString = Main.frame.getAWSSecretKey();
                if (secretKeyString.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Secret key required.  Set it in the menu");
                    return;
                }
                
                if (GlacierFrame.getAWSRegion().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Select a region in the menu.");
                    return;
                }
                //</editor-fold>
                
                //<editor-fold defaultstate="collapsed" desc="get vault">
                String vault = VAULT_INPUT.getText();
                if (vault == null || vault.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Vault name required");
                    return;
                }
                Vault vault_obj = new Vault(vault);
                vault_obj.describe();
                if (!vault_obj.exists()) {
                    JOptionPane.showMessageDialog(null, "Vault '" + vault
                            +"' does not exist in this region");
                    VAULT_INPUT.setText(null);
                    return;
                }
                //</editor-fold>
                final UploadThread worker = new UploadThread();
                worker.withMoveDir(moveFolder.getText())
                    .withVault(vault);
                UPLOAD_BUTTON.setEnabled(false);
                new LaunchUploadProcess(1L * sleepMinute, worker).execute();
            }
        });
        //</editor-fold>
        makeIt((short) 7, (byte) 2);
    }
    
    public static String getDescriptionText(){
        return descriptionTextField.getText();
    }
    
    public static int getZipSize() {
        final String zipString = ZIP_SIZE_INPUT.getText();
        int s;
        
        try{
            s = Integer.parseInt(zipString);
            P.putInt(ZIP_SIZE_KEY, s);
        } catch (NumberFormatException ex) {
            s = 0;
            P.remove(ZIP_SIZE_KEY);
        }
        return s;
    }
       
    public static void setVaultSelection(final String v){
        if (v == null) {
            VAULT_INPUT.setText(null);
        } else if (!v.isEmpty()) {
            VAULT_INPUT.setText(v);
            P.put(VAULTNAME_KEY + GlacierFrame.getAWSRegion(), v);
        }
    }
    
    private ArrayList<String> getFilesInDir(File[] allFiles){
        ArrayList<String> filePaths = new ArrayList<>();
        
         for (int i = 0; i < allFiles.length; i++){
            if (allFiles[i].isDirectory()) {
                filePaths.addAll(getFilesInDir(allFiles[i].listFiles()));
            } else {
                filePaths.add(allFiles[i].getAbsolutePath());
            }
         }
        return filePaths;
    }

    void initVaultInputField() {
        VAULT_INPUT.setText(
                P.get(VAULTNAME_KEY + GlacierFrame.getAWSRegion(), ""));
    }
    
    private class LaunchVaultChooser extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() {
            new CSplash(VAULT_INPUT.getText());
            return null;
        }
    }
    
    private class LaunchUploadProcess extends SwingWorker<Void, Void> {
        private long sleep_minutes;
        private UploadThread upload_thread;
        private LaunchUploadProcess(long minutes, UploadThread worker) {
            upload_thread = worker;
            sleep_minutes = minutes;
        }
        
        @Override
        public Void doInBackground() {
            UploadSplash.getInstance(files_to_upload,
                    1L * sleep_minutes, upload_thread);
            return null;
        }
    }
}
