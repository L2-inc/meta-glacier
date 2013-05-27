/*
 * @(#)GlacierFrame.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import com.vrane.metaGlacier.HumanBytes;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.vrane.desktop.MainFrame;
import com.vrane.encryption.Encryption;
import com.vrane.metaGlacier.Main;
import com.vrane.metaGlacier.gui.search.SearchDialog;
import com.vrane.metaGlacierSDK.GlacierMetadata;
import com.vrane.metaGlacierSDK.MetadataProviderCredentials;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * This JFrame class creates the menu as well as providing an interface to account credentials.
 * Both AWS and metadata account credentials are provided by this interface.
 * It also initializes and saves various user settings. 
 * 
 * @author K Z Win
 */
public class GlacierFrame extends MainFrame
        implements AWSCredentials, MetadataProviderCredentials {
    
    static Encryption encryptionObj;
    
    private final static Logger LGR =
            Main.getLogger(GlacierFrame.class);
    
    private static String accessKeyString = "";
    private static String secretKeyString = "";
    private static String metadataUser = "";
    private static String metadataPassword = "";
    private static String computer_id;
    private static String selectedRegion = "";
    private static Level selectedLogLevel = Level.OFF;
    
    private final static String DEFAULT_COMPUTER_ID
            = "meta-glacier-software-computer";
    private final static String[] LEVEL_STRING 
            = {"off", "warning", "info", "verbose"};
    private final static Level[] LOG_LEVELS
            = {Level.OFF, Level.WARNING, Level.INFO, Level.FINE};
    private final static String SAVED_LOG_LEVEL = "saved log level";
    private final static int MS_IN_A_DAY = 86400000;
    private final static short COMPUTER_ID_LENGTH = 256;
    private final static byte[] ENCRYPTION_KEY
            = {38,-16,16,-45,74,-105,-3,26,10,17};
    private final static String SHOW_LOG_WINDOW_KEY = "hide log panel";
    private final static String AWS_ACCESS_KEY = "aws access";
    private final static String AWS_SECRET_KEY = "aws secret";
    private final static String METADATA_USER_KEY = "metadata username";
    private final static String METADATA_PASSWORD_KEY = "metadata password";
    private final static String REGION_KEY = "region";
    private final static String DISCARD_PHOTO_METADATA = "save photo metadata";
    private final static String COMPUTER_ID_KEY_LOCAL = "computer id";
    private final static String ALWAYS_GET_METADATA = "always get metadata";
    private final static String NUMBER_OF_AWS_API_CALLS
            = "number of aws api calls";
    private final static String GRANULARITY_KEY = "granularity";
    private final static String SHOW_DELETED_KEY = "show deleted";
    private final static MainPanel MAIN_PANEL = new MainPanel();

    private final static String LAST_AWS_API_CALL_RESET
            = "last time stamp of aws api call";
    private final static Preferences P
            = Preferences.userNodeForPackage(GlacierFrame.class);
    private final static String[] REGIONS
            = {"us-west-2", "us-west-1", "us-east-1", "eu-west-1", 
                "ap-northeast-1"};
    private final static String[] REGION_DESC
                = {"US West Oregon", "US West N California",
                    "US East N Virginia", "Ireland", "Tokyo"};
    private final static int[] GRANULARITIES
            = { (int)HumanBytes.MEGA, 2 * (int) HumanBytes.MEGA};
    private final static String[] GRANULARITY_STRINGS
            = { HumanBytes.convert(HumanBytes.MEGA),
                    HumanBytes.convert(HumanBytes.MEGA * 2)};
    private static SearchDialog search_dialog;
    
    private final JMenuItem SHOW_LOG_PANEL_MENU_ITEM
            = new JCheckBoxMenuItem("Show log window");
    private final JMenuItem SHOW_DELETED_ARCHIVES
            = new JCheckBoxMenuItem("Show deleted archives");
    private final JMenuItem alwaysGetMetadataMenuItem
            = new JCheckBoxMenuItem("Always get metadata");
    private final JMenuItem SAVE_PHOTO_MENU
            = new JCheckBoxMenuItem("Save photo-metadata");
    private final JMenuItem USE_SYSTEM_UI
            = new JCheckBoxMenuItem("Use System Theme");
    private final JMenuBar MENU_BAR = new JMenuBar();
    private final JMenu SETUP_PANEL = new JMenu("Set up");
    private final JMenu PREF_PANEL = new JMenu("Preferences");
    private final JMenuItem MANAGE_ACCOUNT = new JMenuItem("AWS account");
    private final JMenu REGION_MENU = new JMenu("Select region");
    private final ButtonGroup REGION_GROUPS = new ButtonGroup();
    private final ButtonGroup GRANULARITY_GROUP = new ButtonGroup();
    private final JMenu GRANULARITY_MENU = new JMenu("Granularity");
    private final JMenuItem metadataSignup
            = new JMenuItem("Sign up metadata account");
    private final JMenuItem COMPUTER_MENUITEM
            = new JMenuItem("Computer identifier");
    private final JMenuItem STAT_RESET_MENUITEM
            = new JMenuItem("Reset statistics");
    private final JMenu AWS_MENU = new JMenu("Glacier");
    private final JMenuItem BROWSE_MENU_ITEM = new JMenuItem("Browse");
    private final JMenuItem SNS_MENU_ITEM
            = new JMenuItem("SNS topic");
    private final JMenuItem SEARCH_MENU_ITEM = new JMenuItem("Search");
    private final JMenu LOG_LEVEL_MENU = new JMenu("Log level");
    private final JMenu LOGGING_MENU= new JMenu("Logging");
    private final JMenuItem META_ACCOUNT_MENU_ITEM
            = new JMenuItem("Metadata account");
    private final JMenuItem ABOUT_MENU_ITEM
            = new JMenuItem("About Meta Glacier");
    private final ButtonGroup LOG_LEVEL_GROUP = new ButtonGroup();

     
    private long mCredentialsSetTime = Long.MIN_VALUE;
    
    private long granularity = 0;
    private final ActionListener metadata_credentials = new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent ae) {
            final AccountDialog account_dialog = 
                    new AccountDialog("user", "password", P,
                    METADATA_USER_KEY, METADATA_PASSWORD_KEY,
                    metadataUser, metadataPassword);
            
            metadataUser = account_dialog.getUser();
            metadataPassword = account_dialog.getPass();
            mCredentialsSetTime = System.currentTimeMillis();
            GlacierMetadata.setCredentials(GlacierFrame.this);
        }
    };

    /**
     * A JFrame constructor.  Program exits when it is closed.
     */
    public GlacierFrame() {
        //Create and set up the window.
        super("Glacier", false);
        String savedRegion = P.get(REGION_KEY, "");
        int savedGranularity = P.getInt(GRANULARITY_KEY, GRANULARITIES[0]);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Add content to the window.
        add(MAIN_PANEL);
        
        //<editor-fold defaultstate="collapsed" desc="add all menu panels">
        MENU_BAR.add(AWS_MENU);
        MENU_BAR.add(SETUP_PANEL);
        MENU_BAR.add(PREF_PANEL);
        //</editor-fold>
        
        /* inventory menu panel begins here*/
        //<editor-fold defaultstate="collapsed" desc="manage vaults menu item">        
        AWS_MENU.add(BROWSE_MENU_ITEM);
        BROWSE_MENU_ITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                new LaunchVaultManager().execute();
            }
        });        
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="search menu item">          
        AWS_MENU.add(SEARCH_MENU_ITEM);
        SEARCH_MENU_ITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!haveMetadataProvider()) {
                    JOptionPane.showMessageDialog(null, "No metadata account");
                    return;
                }
                if (search_dialog == null) {
                    search_dialog = new SearchDialog();
                }
                search_dialog.setVisible(true);
            }
        });
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="create sns topic">                  
        SNS_MENU_ITEM
                .setToolTipText("Create topic and subscribe with email. " +
                    "For download and inventory jobs");
        AWS_MENU.add(SNS_MENU_ITEM);
        SNS_MENU_ITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (!haveAWSCredentials()) {
                    JOptionPane.showMessageDialog(null, "No AWS credentials");
                    return;
                }
                new SNSTopicDialog();
            }
        });
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="vault jobs menu item">   
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="manage account menu item">
        SETUP_PANEL.add(MANAGE_ACCOUNT);
        
        encryptionObj = new Encryption(ENCRYPTION_KEY);
        accessKeyString = P.get(AWS_ACCESS_KEY,"");
        secretKeyString = getPassword(AWS_SECRET_KEY);
        
        MANAGE_ACCOUNT.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                AccountDialog ad =
                        new AccountDialog("access key", "secret key", P,
                        AWS_ACCESS_KEY, AWS_SECRET_KEY, accessKeyString,
                        secretKeyString);
                
                accessKeyString = ad.getUser();
                secretKeyString = ad.getPass();
                if (haveAWSCredentials()) {
                    BROWSE_MENU_ITEM.setEnabled(true);
                } else {
                    BROWSE_MENU_ITEM.setEnabled(false);
                }
            }
        });
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="region menu item">        
        SETUP_PANEL.add(REGION_MENU);
        for (int i = 0; i < REGIONS.length; i++) {
            final int currentIndex = i;
            final JRadioButtonMenuItem rbMenuItem
                    = new JRadioButtonMenuItem(REGION_DESC[i]);
            REGION_GROUPS.add(rbMenuItem);
            if (savedRegion.equals(REGIONS[i])) {
                selectedRegion = savedRegion;
                rbMenuItem.setSelected(true);
            }
            REGION_MENU.add(rbMenuItem);
            rbMenuItem.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    selectedRegion = REGIONS[currentIndex];
                    P.put(REGION_KEY, selectedRegion);
                    rbMenuItem.setSelected(true);
                    BROWSE_MENU_ITEM.setEnabled(haveAWSCredentials());
                    SNS_MENU_ITEM.setEnabled(haveAWSCredentials());
                }
            });
        }
        //</editor-fold>

        SETUP_PANEL.addSeparator();
        
        //<editor-fold defaultstate="collapsed" desc="metadata account menu item">
        SETUP_PANEL.add(META_ACCOUNT_MENU_ITEM);
        metadataUser = P.get(METADATA_USER_KEY, "");
        metadataPassword = getPassword(METADATA_PASSWORD_KEY);
        
        META_ACCOUNT_MENU_ITEM.addActionListener(metadata_credentials);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="metadata account sign up">
        SETUP_PANEL.add(metadataSignup);
        metadataSignup.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                new MetaDataSignUpDialog();
            }
        });
        //</editor-fold>
        SETUP_PANEL.addSeparator();

        //<editor-fold defaultstate="collapsed" desc="'about' menu item">        
        SETUP_PANEL.add(ABOUT_MENU_ITEM);
        ABOUT_MENU_ITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null,
                        "Copyright 2013 Amherst Robots Inc.\n" +
                        "    http://www.vrane.com",
                        "Meta Glacier", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        //</editor-fold>
        /* preferences menu panel begins here */
                                
        //<editor-fold defaultstate="collapsed" desc="granularity menu item">        
        PREF_PANEL.add(GRANULARITY_MENU);
        for (int i = 0; i < GRANULARITIES.length; i++) {
            final int currentIndex = i;
            final JRadioButtonMenuItem rbMenuItem
                    = new JRadioButtonMenuItem(GRANULARITY_STRINGS[i]);
            GRANULARITY_GROUP.add(rbMenuItem);
            GRANULARITY_MENU.add(rbMenuItem);
            if (savedGranularity == GRANULARITIES[i]) {
                rbMenuItem.setSelected(true);
                granularity = savedGranularity;
            }
            rbMenuItem.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    granularity = GRANULARITIES[currentIndex];
                    P.putLong(GRANULARITY_KEY, granularity);
                    rbMenuItem.setSelected(true);
                }
            });
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="logging menu">
        final String savedLogLevel = P.get(SAVED_LOG_LEVEL, "");
        PREF_PANEL.add(LOGGING_MENU);
        LOGGING_MENU.add(LOG_LEVEL_MENU);
        LOGGING_MENU.add(SHOW_LOG_PANEL_MENU_ITEM);
        SHOW_LOG_PANEL_MENU_ITEM.setSelected(
                P.getBoolean(SHOW_LOG_WINDOW_KEY, false));
        SHOW_LOG_PANEL_MENU_ITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (SHOW_LOG_PANEL_MENU_ITEM.isSelected()) {
                    P.putBoolean(SHOW_LOG_WINDOW_KEY, true);
                    Main.logwindow.setVisible(true);
                } else {
                    P.remove(SHOW_LOG_WINDOW_KEY);
                    Main.logwindow.setVisible(false);
                }
            }
        });
        for (int i = 0; i < LEVEL_STRING.length; i++) {
            final JRadioButtonMenuItem radioButtonLogLevelMenuItem =
                    new JRadioButtonMenuItem(LEVEL_STRING[i]);
            final int current_index = i;

            LOG_LEVEL_GROUP.add(radioButtonLogLevelMenuItem);
            LOG_LEVEL_MENU.add(radioButtonLogLevelMenuItem);
            radioButtonLogLevelMenuItem.setActionCommand(LEVEL_STRING[i]);
            if (savedLogLevel.equals(LEVEL_STRING[i])) {
                radioButtonLogLevelMenuItem.setSelected(true);
                selectedLogLevel = LOG_LEVELS[i];
            }
            radioButtonLogLevelMenuItem.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    final String ac = ae.getActionCommand();
                    
                    P.put(SAVED_LOG_LEVEL, ac);
                    selectedLogLevel = LOG_LEVELS[current_index];
                    Logger.getLogger("").setLevel(selectedLogLevel);
                    for (final Handler hl: Logger.getLogger("").getHandlers()) {
                        hl.setLevel(selectedLogLevel);
                    }
                    final boolean hide_window
                            = selectedLogLevel.equals(Level.OFF);
                    if (hide_window) {
                        P.remove(SHOW_LOG_WINDOW_KEY);
                    }
                    Main.logwindow.setVisible(!hide_window);
                }
            });
            
        }
        Logger.getLogger("").setLevel(selectedLogLevel);
        for (final Handler hdlr: Logger.getLogger("").getHandlers()) {
            hdlr.setLevel(selectedLogLevel);
        }
        if (!selectedLogLevel.equals(Level.OFF) &&
                SHOW_LOG_PANEL_MENU_ITEM.isSelected()) {
            Main.logwindow.setVisible(true);
        }
        Main.logwindow.addWindowListener(new WindowAdapter(){
                
            @Override
            public void windowClosing(WindowEvent e) {
                SHOW_LOG_PANEL_MENU_ITEM.setSelected(false);
            }
        });
        //</editor-fold>

        PREF_PANEL.addSeparator();
        
        //<editor-fold defaultstate="collapsed" desc="show deleted archives menu item">        
        PREF_PANEL.add(SHOW_DELETED_ARCHIVES);
        if (P.getBoolean(SHOW_DELETED_KEY, false)) {
            SHOW_DELETED_ARCHIVES.setSelected(true);
        }
        SHOW_DELETED_ARCHIVES.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (SHOW_DELETED_ARCHIVES.isSelected()) {
                    P.putBoolean(SHOW_DELETED_KEY, true);
                } else {
                    P.remove(SHOW_DELETED_KEY);
                }
            }
        });
        
        //</editor-fold>

        PREF_PANEL.addSeparator();

        //<editor-fold defaultstate="collapsed" desc="save photo metadata menu item">
        PREF_PANEL.add(SAVE_PHOTO_MENU);
        if (P.getBoolean(DISCARD_PHOTO_METADATA, true)) {
            SAVE_PHOTO_MENU.setSelected(true);
        }
        SAVE_PHOTO_MENU.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (SAVE_PHOTO_MENU.isSelected()) {
                    P.remove(DISCARD_PHOTO_METADATA);
                } else {
                    P.putBoolean(DISCARD_PHOTO_METADATA,true);
                }
            }
        });
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="computer id menu item">
        PREF_PANEL.add(COMPUTER_MENUITEM);
        COMPUTER_MENUITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                final JDialog cidDialog = new JDialog(Main.frame, true);
                JPanel accountPanel = new JPanel(new GridLayout(0, 1, 10, 10));
                final JTextField cidJT = new JTextField(computer_id);
                JButton setButton = new JButton("Set Computer ID");
                
                accountPanel.setBorder(
                        BorderFactory.createEmptyBorder(10, 5, 5, 5));
                accountPanel.add(cidJT);
                accountPanel.add(setButton);
                setButton.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        computer_id = cidJT.getText();
                        if (computer_id.length() > COMPUTER_ID_LENGTH) {
                            computer_id = computer_id.substring(0,
                                                        COMPUTER_ID_LENGTH);
                        }
                        P.put(COMPUTER_ID_KEY_LOCAL, computer_id);
                        cidDialog.dispose();
                    }
                });
                cidDialog.add(accountPanel);
                cidDialog.pack();
                cidDialog.setLocationRelativeTo(Main.frame);
                cidDialog.setVisible(true);
            }
        });
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="reset stats menu item">
        PREF_PANEL.add(STAT_RESET_MENUITEM);
        STAT_RESET_MENUITEM.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                P.remove(LAST_AWS_API_CALL_RESET);
                P.remove(NUMBER_OF_AWS_API_CALLS);
                GlacierMetadata.resetCounter();
            }
        });
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="UI theme menu item">
        if(System.getProperty("os.name").startsWith("Windows ")){
            USE_SYSTEM_UI.setToolTipText("Requires restart");
            PREF_PANEL.add(USE_SYSTEM_UI);
            USE_SYSTEM_UI.setSelected(Main.wantNativeTheme());
            USE_SYSTEM_UI.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    Main.setNativeThemeFlag(USE_SYSTEM_UI.isSelected());
                }
            });
            USE_SYSTEM_UI.setToolTipText("changes require restart");
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="always get metadata">
        PREF_PANEL.add(alwaysGetMetadataMenuItem);
        alwaysGetMetadataMenuItem.setSelected(P.getBoolean(ALWAYS_GET_METADATA, false));
        alwaysGetMetadataMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                P.putBoolean(ALWAYS_GET_METADATA, 
                        alwaysGetMetadataMenuItem.isSelected());
            }
        });
        //</editor-fold>

        setJMenuBar(MENU_BAR);
        /* The following two items need region value set up */
        BROWSE_MENU_ITEM.setEnabled(haveAWSCredentials());
        SNS_MENU_ITEM.setEnabled(haveAWSCredentials());

        pack();
        setResizable(false);
        setComputerId(P.get(COMPUTER_ID_KEY_LOCAL, null));
        MAIN_PANEL.initVaultInputField();
    }

    /**
     * Indicates whether to get confirmation from user to get metadata if there
     * is no AWS inventory data.
     * 
     * @return true if user always want to get data from metadata.
     */
    public boolean doNotConfirmGettingMetadata() {
        return alwaysGetMetadataMenuItem.isSelected();
    }

    /**
     * Gets a string which identifies the computer doing the upload to metadata
     * server.
     *
     * @return computer id string to be stored with uploaded archive.
     */
    public static String getComputerId() {
        return computer_id;
    }

    /**
     * Gets the number of AWS calls since last reset.
     *
     * @return AWS API call
     */
    public static int getAWSCalls() {
        return P.getInt(NUMBER_OF_AWS_API_CALLS, 0);
    }

    /**
     * Gets the number of days since AWS API call number was reset.
     *
     * @return number of days as string
     */
    public static String getAWSapiLastReset() {
        return getNumberOfDays(P.getLong(LAST_AWS_API_CALL_RESET,
                System.currentTimeMillis()));
    }

    /**
     * Gets the number of days since metadata API call number was reset.
     *
     * @return number of days as string
     */
    public static String getAPILastReset(){
        return getNumberOfDays(GlacierMetadata.getAPICounterResetTime());
    }
    
    private static String getNumberOfDays(final long since){
        final int days
                = (int) (System.currentTimeMillis() - since) / MS_IN_A_DAY;
        
        return "Reset " + days + " day" + ((days > 1) ? "s" : "" ) + " ago";        
    }
    
    private void setComputerId(String cid){
        if (cid == null) {
            try {
                cid = System.getProperty("os.name") + ' ' 
                        + System.getProperty("os.arch")
                        + ' ' + System.getProperty("os.version") + ' '
                        + java.net.InetAddress.
                        getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException ex) {
                LGR.log(Level.SEVERE, null, ex);
                LGR.severe("Cannot determine computer id automatically");
                computer_id = DEFAULT_COMPUTER_ID;
            }
            if (COMPUTER_ID_LENGTH < cid.length()) {
                cid = cid.substring(0, COMPUTER_ID_LENGTH);
            }
            P.put(COMPUTER_ID_KEY_LOCAL, cid);
        }
        computer_id = cid;
    }

    /**
     * Indicates whether to show deleted archives.
     *
     * @return true if user wants to see deleted archives; false by default.
     */
    public boolean showDeleted() {
        return SHOW_DELETED_ARCHIVES.isSelected();
    }

    /**
     * Indicates whether user wants to save photo metadata in the cloud.
     *
     * @return false if user does not want to store photo metadata;
     * true by default.
     */
    public boolean canSavePhotoMetadata() {
        return SAVE_PHOTO_MENU.isSelected();
    }
    
    private String getPassword(final String _KEY) {
        final String encPass = P.get(_KEY, null);
        
        return ( encPass == null )
                ? null : encryptionObj.decrypt(encPass);
    }

    /**
     * Gets the currently selected AWS region.
     *
     * @return string such as 'us-west-2', 'eu-west-1'
     */
    public static String getAWSRegion(){
        return selectedRegion;
    }

    /**
     * Gets AWS Access Key.
     *
     * @return access key string
     */
    @Override
    public String getAWSAccessKeyId() {
        return accessKeyString;
    }

    /**
     * Gets AWS Secret Key.
     *
     * @return secret key string
     */
    @Override
    public String getAWSSecretKey() {
        return secretKeyString;
    }

    /**
     * Gets the upload size used during multipart upload.
     *
     * @return size in bytes
     */
    public long getGranularity() {
        return granularity;
    }

    /**
     * Returns AWS client in the current region.
     *
     * @return AWS client object
     */
    public static AmazonGlacierClient getClient(){
        return getClient(getAWSRegion());
    }

    /**
     * Returns the AWS client in the specified region.
     *
     * @param region string such as 'us-west-1', 'eu-west-1'
     * @return AWS client object
     */
    public static AmazonGlacierClient getClient(final String region){
            final AmazonGlacierClient client = new AmazonGlacierClient(Main.frame);
        int current_api_call_count = P.getInt(NUMBER_OF_AWS_API_CALLS, 0);
        final String endpointURL = "https://glacier.%s.amazonaws.com";
        
        client.setEndpoint(String.format(endpointURL, region));
        if (current_api_call_count == 0) {
            P.putLong(LAST_AWS_API_CALL_RESET, System.currentTimeMillis());
        }
        P.putInt(NUMBER_OF_AWS_API_CALLS, 1 + current_api_call_count);
        return client;    
    }

    /**
     * Indicates whether metadata account is setup.
     * 
     * @return true if metada credentials are available.
     */
    public static boolean haveMetadataProvider(){
        return (metadataUser != null && !metadataUser.isEmpty() 
                && metadataPassword != null && !metadataPassword.isEmpty());
    }

    /**
     * Make a JLabel with fix sized label string.
     *
     * @param n string to be used in label
     * @param len at most this many characters in this label to be used.
     * @return JLabel
     */
    public static JLabel makeLabelWithLength(String n, final byte len) {
        String dn = n;
        
        if (n.length() > len) {
            n = n.substring(0, len-4) + " ...";
        }
        JLabel label = new JLabel(n);
        if (!n.equals(dn)) {
            label.setToolTipText(dn);
        }
        return label;
    }

    /**
     * Make a JLabel with label string of at most 20 characters.
     *
     * @param string label string
     * @return JLabel
     */
    public static JLabel makeLabel(String string) {
        return makeLabelWithLength(string, (byte) 20);
    }

    /**
     * Returns metadata account user string.
     *
     * @return user string
     */
    @Override
    public String getMPCUser() {
        return metadataUser;
    }

    /**
     * Returns metadata account password string.
     *
     * @return password string
     */
    @Override
    public String getMPCPass() {
        return metadataPassword;
    }

    /**
     * Time when metadata credentials are set.
     *
     * @return unix epoch in millisecond
     */
    @Override
    public long lastMPCSet() {
        return mCredentialsSetTime;
    }

    /**
     * Indicates whether AWS credentials are available.
     *
     * @return false if there is no AWS credentials.
     */
    private boolean haveAWSCredentials() {
        return secretKeyString != null && accessKeyString != null
                && !accessKeyString.isEmpty() && !secretKeyString.isEmpty()
                && selectedRegion != null && !selectedRegion.isEmpty();
    }
        
    private class LaunchVaultManager extends SwingWorker<Void, Void> {
        
        @Override
        public Void doInBackground() {
            new VSplash();
            return null;
        }
    }
}