/*
 * @(#)Main.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import com.vrane.desktop.MainFrame;
import com.vrane.metaGlacier.gui.GlacierFrame;
import com.vrane.metaGlacierSDK.GlacierMetadata;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This is the entry class.
 * It starts a JFrame, set up metadata credential, and provide a method to get
 * a logger instance.
 * 
 * @author K Z Win
 * @version     %I%, %G%
 */

public class Main {

    /**
     * The main window; closing it exits the program.
     */
    public static GlacierFrame frame;

    private final static Preferences P =
            Preferences.userNodeForPackage(Main.class);
    /**
     * Window with log messages.
     */
    public final static LogWindow logwindow = new LogWindow();

    private final static String NATIVE_LOOK = "native look and feel";
    
    private final static GLogHandler gloghandler = new GLogHandler();
    private final static byte LOG_NAME_START = (byte) "com.vrane.".length();
    private final static Logger LGR = Logger.getLogger(Main.class.getName());
    private final static int PORT = 8866;

    /**
     * Main entry point.
     * 
     * @param args command line arguments not used.
     */
    public static void main(String[] args) {
//        if(System.getProperty("os.name").startsWith("Mac OS ")){
//            System.setProperty("apple.laf.useScreenMenuBar", "true");
//        }
        //<editor-fold defaultstate="collapsed" desc="get a lock to prevent multiple instances on one computer">
          try{
              LGR.log(Level.INFO, "Binding to port number {0}", PORT);
              new ServerSocket(PORT, 1, InetAddress.getByName("127.0.0.1"));
          } catch(IOException ie) {
              LGR.warning("This program is already running!");
              LGR.log(Level.WARNING,
                      "Or another program is using port number {0}!", PORT);
              System.exit(0);
          }
          //</editor-fold>
        frame = new GlacierFrame();
        GlacierMetadata.setCredentials(frame);
        frame.setVisible(true);
    }

    /**
     * Gets the logger object.
     * 
     * @param cl is the calling class
     * @return Logger object with a log handler added to it.
     */
    public static Logger getLogger(final Class cl){
        final Logger lgr = Logger.getLogger(cl.getName());
        lgr.addHandler(gloghandler);
        return lgr;
    }

    /**
     * Does nothing on Mac.  On windows, set a flag on whether to use Windows
     * theme to draw windows components.
     *
     * @param flag supply false on windows if Java 'metal' theme is wanted.
     */
    public static void setNativeThemeFlag(final boolean flag) {
        P.putBoolean(NATIVE_LOOK, flag);
    }

    /**
     * On windows, indicates whether Windows theme should be used to draw UI.
     *
     * @return true if Windows theme is wanted.
     */
    public static boolean wantNativeTheme() {
        return P.getBoolean(NATIVE_LOOK, false);
    }

    private static class GLogHandler extends Handler {

        @Override
        public void publish(LogRecord lr) {
            final String mes = lr.getMessage();
            final Object[] params = lr.getParameters();
            final String log_message = mes == null
                    ? (params == null ) ? null : params.toString()
                    : params == null
                    ? mes
                    : MessageFormat.format(mes, params);
            final String logger_name
                    = lr.getLoggerName().substring(LOG_NAME_START);
            final Level log_level = lr.getLevel();
            String level_string = log_level.getLocalizedName();
            final Throwable t = lr.getThrown();
            
            if (log_level.intValue() > Level.INFO.intValue()){
                level_string += " " + new Date().toString();
            }
            logwindow.say(logger_name);
            if (log_message != null) {
                logwindow.say(level_string + ": " + log_message);
            }
            if (t == null) {
                return;
            }
            if (t.getMessage() != null) {
                logwindow.say(t.getMessage());
            }
            StackTraceElement[] ste = t.getStackTrace();
            if (ste != null) {
                for (StackTraceElement st: ste) {
                    logwindow.say("   " + st.toString());
                }
            }
            Throwable c = t.getCause();
            if (c == null) {
                return;
            }
            StackTraceElement[] ste1 = c.getStackTrace();
            if (ste1 == null) {
                return;
            }
            logwindow.say("caused by...");
            for (StackTraceElement st: ste1) {
                logwindow.say("   " + st.toString());
            }
        }

        @Override
        public void flush() {
            logwindow.clear();
        }

        @Override
        public void close() throws SecurityException {
            logwindow.clear();
        }
        
    }

    /**
     * This represents the window where log messages are shown.
     */
    public static class LogWindow extends MainFrame {
        private final JTextArea logg = new JTextArea(25, 60);
        
        LogWindow(){
            super("log window", P.getBoolean(NATIVE_LOOK, false));
            final JScrollPane logScrollPane = new JScrollPane(logg);
            final JPanel panl = new JPanel(new BorderLayout());
            final JButton clearButton = new JButton("clear");
            
            logg.setLineWrap(true);
            logg.setMargin(new Insets(5, 5, 5, 5));
            logg.setEditable(false);
            panl.add(logScrollPane, BorderLayout.NORTH);
            panl.add(clearButton, BorderLayout.SOUTH);
            add(panl);
            setResizable(false);
            clearButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    logg.setText("");
                }
            });
            pack();
        }

        void say(final String message){
            logg.append(message + "\n");
        }

        void clear(){
            logg.setText("");
        }
    }
    
}
