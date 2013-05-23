/*
 * @(#)Utilities.java  0.6 2013 May 5
 *
 * Copyright (c) 2013 Amherst Robots, Inc.
 * All rigts reserved.
 *
 * See LICENSE file accompanying this file.
 */
package com.vrane.metaGlacier;

import java.text.DecimalFormat;

/**
 * Contains a single method used by a few other classes.
 *
 * @author K Z Win
 */
public class Utilities {

    /**
     * Gets file transfer rate as formatted string and time to finish the
     * transfer.
     *
     * @param rate_in_kb float in kB/s
     * @param remain_bytes remaining bytes to finish the transfer
     * @return array of string; first element is formatted rate in kB/s and the
     * second element is time to finish
     */
    public static String[] getRateInfo(final float rate_in_kb, final long remain_bytes) {
        DecimalFormat myFormatter = new DecimalFormat("####.#");
        String rateString = myFormatter.format(rate_in_kb);
        int time_in_s = (int) (remain_bytes / rate_in_kb / HumanBytes.KILO);
        String time_to_upload;
        
        if (time_in_s < 60) {
            time_to_upload = setUnit((byte) time_in_s, "second");
        } else if (time_in_s >= 60 && time_in_s < 3600) {
            time_to_upload = setUnit((byte) (time_in_s / 60), "minute");
            if (time_in_s < 400) {
                time_to_upload += " " + setUnit((byte) (time_in_s % 60), "second");
            }
        } else {
            time_to_upload = setUnit((byte) (time_in_s / 3600), "hour");
            if (time_in_s < 10000) {
                time_to_upload += " " + setUnit((byte) ((time_in_s % 3600) / 60), "minute");
            }
        }
        return new String[]{rateString, time_to_upload};
    }

    private static String setUnit(final byte number, final String unit) {
        String num_with_string = number + " " + unit;
        
        if (number > 1) {
            num_with_string += "s";
        }
        return num_with_string;
    }

}
