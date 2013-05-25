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
     * @param position is the current file position
     */
    abstract void setFilePosition(final long position);

    /**
     * Sets the file size.
     * 
     * @param size the file size in bytes
     */
    abstract void setFileSize(final long size);

    /**
     * Reports the total size uploaded so far for all files by passing the
     * position of the current file.
     * Implement this only if your class involves handling multiple files;
     * otherwise implement as no-ops.
     *
     * @param pos is the file position in bytes of the current file.
     */
    abstract void updateTotalSize(final long pos);
    
}
