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
 *
 * @author K Z Win
 */
public class Utilities {

    public static String[] getRateInfo(final float rate_in_kb, final long size_in_bytes) {
        DecimalFormat myFormatter = new DecimalFormat("####.#");
        String rateString = myFormatter.format(rate_in_kb);
        int time_in_s = (int) (size_in_bytes / rate_in_kb / HumanBytes.KILO);
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

    static String setUnit(final byte number, final String unit) {
        String num_with_string = number + " " + unit;
        if (number > 1) {
            num_with_string += "s";
        }
        return num_with_string;
    }

}
