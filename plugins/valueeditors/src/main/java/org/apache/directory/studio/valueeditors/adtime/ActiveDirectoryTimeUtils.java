/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.directory.studio.valueeditors.adtime;


import java.util.Calendar;


// ── CLASS: ActiveDirectoryTimeUtils — R2-D2 COMPUTING ELAPSED TIME FROM TATOOINE
// R2-D2 knows that the Empire uses a different time-base than the Rebels —
// Imperial chronometers count 100-nanosecond ticks from January 1, 1601 (Windows
// FILETIME), while everyone else counts milliseconds from January 1, 1970 (Unix epoch).
// When Luke asks "what time is it?", R2-D2 converts the Imperial tick count into
// a human-readable date by subtracting the 369-year offset and rescaling.
// This class is R2-D2's time-translation subroutine.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility methods for converting between Active Directory (Windows FILETIME)
 * timestamps and Java {@link Calendar} objects.
 * Active Directory stores timestamps as 64-bit integers counting
 * 100-nanosecond intervals since 1 January 1601.  Java counts milliseconds since
 * 1 January 1970.  Converting between them requires accounting for the 369-year
 * gap (116,444,736,000,000,000 100-ns ticks) and the scale factor (10,000 ticks/ms).
 * Think of this class as R2-D2's time-conversion module — he takes the Empire's
 * alien tick count and turns it into something the crew can actually read.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ActiveDirectoryTimeUtils
{
    // ── R2-D2 Decodes the Imperial Timestamp ─────────────────────────────────
    // Luke hands R2-D2 an Imperial FILETIME value from the captured datapad.
    // R2-D2 subtracts the 369-year epoch offset, rescales from 100-ns to ms,
    // and outputs the result as a readable calendar date for the mission log.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts an Active Directory FILETIME value to a Java {@link Calendar}.
     * The AD FILETIME format counts 100-nanosecond intervals since 1 Jan 1601;
     * we subtract the 369-year offset (116,444,736,000,000,000 ticks) and divide
     * by 10,000 to get Unix milliseconds, then wrap that in a Calendar.
     *
     * <p>For example — R2-D2 decodes a login timestamp:</p>
     * <pre>
     *   long adTime = 133000000000000000L; // some AD FILETIME
     *   Calendar cal = ActiveDirectoryTimeUtils.convertToCalendar(adTime);
     *   // cal.getTime() → readable Java Date
     * </pre>
     *
     * @param adTimeValue  The Active Directory FILETIME (100-ns ticks since 1601-01-01).
     * @return             A {@code Calendar} representing the same moment in Java time.
     */
    public static Calendar convertToCalendar( long adTimeValue )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( ( adTimeValue - 116444736000000000L ) / 10000L );
        return calendar;
    }


    // ── R2-D2 Re-encodes the Date as Imperial FILETIME ────────────────────────
    // The mission log records a date in standard Rebel time; R2-D2 must write it
    // back into the captured Imperial datapad using FILETIME ticks.
    // He multiplies the Java milliseconds by 10,000 and adds the 369-year offset.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts a Java {@link Calendar} to an Active Directory FILETIME value.
     * We take Java's milliseconds-since-1970 value, scale up to 100-ns ticks
     * (multiply by 10,000), then add the 369-year epoch offset to land in
     * AD's 1601 epoch.
     *
     * <p>For example — R2-D2 re-encodes a date for the Imperial datapad:</p>
     * <pre>
     *   Calendar now = Calendar.getInstance();
     *   long adTime = ActiveDirectoryTimeUtils.convertToActiveDirectoryTime(now);
     *   // adTime is now a valid AD FILETIME
     * </pre>
     *
     * @param calendar  The Java Calendar to convert.
     * @return          The equivalent Active Directory FILETIME value.
     */
    public static long convertToActiveDirectoryTime( Calendar calendar )
    {
        return ( ( calendar.getTime().getTime() * 10000L ) + 116444736000000000L );
    }
}
