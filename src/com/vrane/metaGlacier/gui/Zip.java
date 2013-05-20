/*
 * @(#)Zip.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier.gui;

/*
 * This class create a zip file from a list of files.
 * TODO: move this to the using class?
 */
import com.vrane.metaGlacier.HumanBytes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class Zip {
    
    private String fileName;
    
    Zip(final String seed){
        fileName = seed;
    }
    
    String zip(String[] files) throws IOException{
        if (fileName.length() < 3) {
            // This handles createTempFile limitation
            fileName = fileName + "_glacier_";
        }
        File temp = File.createTempFile(fileName, ".zip");
        temp.deleteOnExit();
        try (ZipOutputStream zos
                = new ZipOutputStream(new FileOutputStream(temp))) {
            byte[] buf = new byte[(int) HumanBytes.KILO];
            for (String filename: files) {
                try (FileInputStream fis = new FileInputStream(filename)) {
                    zos.putNextEntry(new ZipEntry(filename));
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
        return temp.getAbsolutePath();
    }
}