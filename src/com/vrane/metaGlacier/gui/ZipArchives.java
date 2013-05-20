/*
 * @(#)ZipArchives.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
/**
 * This class represents zipping up a set of files.
 * The final zip file is to be sent to AWS as an archive.
 * It exposes <i>zipList</i> public variable as <br>
 * &nbsp;&nbsp;<code>HashMap&lt;String, ArrayList&lt;String&gt;&gt</code>
 * @author K Z Win
 */
class ZipArchives {
    
    
    public final HashMap<String,ArrayList<String>> zipList = new HashMap();
    private final HashMap<String, Long> highTime = new HashMap();
    private final HashMap<String, Long> lowTime = new HashMap();

    public ZipArchives(final ArrayList<File> allFiles, long zipSize,
            final String zipFileNameSeed){
        Long currentSize=0L;
        ArrayList<String> currentList = new ArrayList<>();
        int zipIndex = 0;

        // TODO: a better algorithm??
        File[] fileArray = allFiles.toArray(new File[0]);
        Arrays.sort(fileArray,new Comparator<File>(){
            @Override
            public int compare (File a, File b){
                Long la = a.length();
                Long lb = b.length();
                return (la < lb) ? -1 : (la > lb) ? 1 : 0;
            }
        });

        for (final File f: fileArray) {
            final String path = f.getAbsolutePath();

            currentSize += f.length();
            if (currentSize> zipSize) {
                String zipName = zipFileNameSeed + zipIndex++;
                ArrayList<String> tempList;
                if (currentList.isEmpty()) {
                    currentList.add(path);
                    tempList = (ArrayList<String>) currentList.clone();
                    currentSize = 0L;
                    currentList.clear();
                } else {
                    tempList = (ArrayList<String>) currentList.clone();
                    currentList.clear();
                    currentList.add(path);
                    currentSize = f.length();
                }
                zipList.put(zipName, tempList);
                continue;
            }
            currentList.add(path);
        }
        if (currentSize > 0L) {
            String zipName = zipFileNameSeed + zipIndex;
            zipList.put(zipName, currentList);
        }
    }
    
    long getLowModTime(String path){
        if (lowTime.isEmpty()) {
            initModHashes();
        }
        return lowTime.get(path);
    }
    
    long getHighModTime(String path){
        if (highTime.isEmpty()) {
            initModHashes();
        }
        return highTime.get(path);
    }
    
    private void initModHashes(){
        for (Map.Entry<String,ArrayList<String>> entry : zipList.entrySet()) {
            long low = Long.MAX_VALUE;
            long high = Long.MIN_VALUE;
            for (String p: entry.getValue()) {
                File f = new File(p);
                Long modifiedTime = f.lastModified();
                if (modifiedTime > high) {
                    high = modifiedTime;
                }
                if (modifiedTime < low) {
                    low = modifiedTime;
                }
            }
            String zipPath = entry.getKey();
            lowTime.put(zipPath, low);
            highTime.put(zipPath, high);
        }        
    }
    
}