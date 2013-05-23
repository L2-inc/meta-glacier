/*
 * @(#)HumanBytes.java  0.6 2013 May 5
 * 
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 * 
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import java.text.DecimalFormat;

/**
 * Use this to convert bytes to kB, MB, GB, TB, PB, EB.
 *
 * @see <b>convert()</b>
 * @author K Z Win
 */
public class HumanBytes {
    /**
     * Number 1024
     */
    public static final long KILO = 1024;

    /**
     * Number 1024 * 1024
     */
    public static final long MEGA = KILO * KILO;
    private static final long GIGA = MEGA * KILO;
    private static final long TERA = MEGA * MEGA;
    private static final long PETA = TERA * KILO;
    private static final long EXA = PETA * KILO;

    /**
     * Supply bytes in long and it returns the size with unit kB, MB etc as a
     * string. Only one digit after the decimal is preserved.
     * 
     * @param _bytes in bytes
     * @return formatted number appended with one of strings
     *     "kB", "MB", "GB", "TB", "PB" or "EB"
     */
    public static String convert(final long _bytes){
        String answer;
        final DecimalFormat form = new DecimalFormat(".#");
        final double b = (double) _bytes;

        if (b >= EXA) {
            answer = form.format(b / EXA) + " EB";
        } else if (b >= PETA) {
            answer = form.format(b / PETA) +" PB";
        } else if (b >= TERA) {
            answer = form.format(b / TERA) + " TB";
        } else if (b >= GIGA) {
            answer = form.format(b / GIGA) + " GB";
        } else if (b >= MEGA) {
            answer = form.format(b / MEGA) + " MB";
        } else if (b >= KILO) {
            answer = form.format(b / KILO) + " kB";
        } else {
            answer = b + " B";
        }
        return answer;
    }
}
