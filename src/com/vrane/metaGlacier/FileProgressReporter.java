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
 * Implement this to receive file progress from an long running process like
 * upload or download.
 * 
 * @author K Z Win
 */
public interface FileProgressReporter {
    
    abstract void setFilePosition(final long position);
    
    abstract void setFileSize(final long size);

    abstract void updateTotalSize(final long pos);
    
}
