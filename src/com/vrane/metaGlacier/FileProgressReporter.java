/*
 * @(#)FileProgressReporter.java  0.6 2013 May 14
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

/**
 * Implement this to receive file progress from a long running process like
 * upload or download.
 * 
 * @author K Z Win
 */
public interface FileProgressReporter {

    /**
     * Sets the current file position in bytes.
     * 
     * @param position
     */
    abstract void setFilePosition(final long position);

    /**
     * Sets the total file size.
     * 
     * @param size
     */
    abstract void setFileSize(final long size);

    /**
     * Sets total size of all files by passing the position of the current file.
     * Implement this only if your class involves handling multiple files.
     *
     * @param pos is the current file position in bytes.
     */
    abstract void updateTotalSize(final long pos);
    
}
