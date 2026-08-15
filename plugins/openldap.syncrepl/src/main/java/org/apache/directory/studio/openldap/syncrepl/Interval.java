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
package org.apache.directory.studio.openldap.syncrepl;


import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// ── CLASS: Interval — The Scheduled Intelligence Dispatch Timetable ───────────
// The Empire's sector command receives a fresh intelligence package from Death
// Star HQ at a fixed schedule: every 00 days, 01 hour, 00 minutes, 00 seconds
// for instance.  The schedule is expressed as "dd:hh:mm:ss" and it governs how
// often a consumer LDAP server polls its provider in "refreshOnly" sync mode.
// This class parses that schedule string, stores the four components, and
// serialises them back to the same "dd:hh:mm:ss" format.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Models the syncrepl {@code interval} parameter.
 * An interval specifies how often the consumer polls the provider for changes
 * when operating in "refreshOnly" replication mode.
 * Format: {@code "dd:hh:mm:ss"} where each component is a zero-padded two-digit integer.
 * For example — an interval of one hour would be written as {@code "00:01:00:00"}.
 * Think of this as the scheduled timetable for Imperial intelligence dispatches:
 * every N days, hours, minutes, and seconds.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Interval
{
    /** The pattern used for parsing */
    private static final Pattern pattern = Pattern.compile( "^([0-9]{2}):([0-9]{2}):([0-9]{2}):([0-9]{2})$" );

    /** The days */
    private int days;

    /** The hours */
    private int hours;

    /** The minutes */
    private int minutes;

    /** The seconds */
    private int seconds;


    // ── Empty Timetable Created — All Zeros by Default ────────────────────────
    // A blank interval with all fields at zero; the caller sets individual
    // components (days, hours, minutes, seconds) afterwards.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a zero-valued interval.
     * All fields default to 0 — use the setters or the four-argument constructor
     * to populate them.
     */
    public Interval()
    {
    }


    // ── Full Timetable Specified at Creation ──────────────────────────────────
    // The dispatch scheduler supplies all four components at once when the
    // interval is known upfront.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an interval with all four components set.
     *
     * @param days     the day component (0–99).
     * @param hours    the hour component (0–23, though OpenLDAP accepts larger values).
     * @param minutes  the minute component (0–59).
     * @param seconds  the second component (0–59).
     */
    public Interval( int days, int hours, int minutes, int seconds )
    {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }


    // ── Copy the Timetable for a Different Sector ─────────────────────────────
    // Imperial HQ sends the same timetable to multiple sector commands — we
    // deep-copy the interval so each command has its own independent schedule.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of the given interval, or {@code null} if the input is {@code null}.
     *
     * @param interval  the interval to copy.
     * @return          a new {@link Interval} with the same component values.
     */
    public static Interval copy( Interval interval )
    {
        if ( interval != null )
        {
            Interval intervalCopy = new Interval();

            intervalCopy.setDays( interval.getDays() );
            intervalCopy.setHours( interval.getHours() );
            intervalCopy.setMinutes( interval.getMinutes() );
            intervalCopy.setSeconds( interval.getSeconds() );

            return intervalCopy;
        }

        return null;
    }


    // ── Copy This Timetable ───────────────────────────────────────────────────
    // Convenience instance method that delegates to the static copy().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this interval.
     *
     * @return  a new {@link Interval} with the same component values.
     */
    public Interval copy()
    {
        return Interval.copy( this );
    }


    // ── Decode the Timetable from the Configuration String ────────────────────
    // The OpenLDAP configuration file stores the interval as a "dd:hh:mm:ss"
    // string — we match each group with a regex and parse the integers out.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses an interval string in "dd:hh:mm:ss" format.
     * Each component must be exactly two digits.  We parse them as integers
     * and store them in the returned object.
     *
     * @param s  the interval string, e.g. {@code "00:01:00:00"}.
     * @return   the parsed {@link Interval}.
     * @throws ParseException  if the string doesn't match the expected format
     *                         or if any component can't be parsed as an integer.
     */
    public static Interval parse( String s ) throws ParseException
    {
        // Creating the interval
        Interval interval = new Interval();

        // Matching the string
        Matcher matcher = pattern.matcher( s );

        // Checking the result
        if ( matcher.find() )
        {
            // Days
            String days = matcher.group( 1 );

            try
            {
                interval.setDays( Integer.parseInt( days ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert days value '" + days + "' as an integer.", 0 );
            }

            // Hours
            String hours = matcher.group( 2 );

            try
            {
                interval.setHours( Integer.parseInt( hours ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert hours value '" + hours + "' as an integer.", 0 );
            }

            // Minutes
            String minutes = matcher.group( 3 );

            try
            {
                interval.setMinutes( Integer.parseInt( minutes ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert minutes value '" + minutes + "' as an integer.", 0 );
            }

            // Seconds
            String seconds = matcher.group( 4 );

            try
            {
                interval.setSeconds( Integer.parseInt( seconds ) );
            }
            catch ( NumberFormatException e )
            {
                throw new ParseException( "Unable to convert seconds value '" + seconds + "' as an integer.", 0 );
            }
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid interval.", 0 );
        }

        return interval;
    }


    /**
     * Returns the day component of this interval.
     *
     * @return  the number of days.
     */
    public int getDays()
    {
        return days;
    }


    /**
     * Returns the hour component of this interval.
     *
     * @return  the number of hours.
     */
    public int getHours()
    {
        return hours;
    }


    /**
     * Returns the minute component of this interval.
     *
     * @return  the number of minutes.
     */
    public int getMinutes()
    {
        return minutes;
    }


    /**
     * Returns the second component of this interval.
     *
     * @return  the number of seconds.
     */
    public int getSeconds()
    {
        return seconds;
    }


    /**
     * Sets the day component.
     *
     * @param days  the number of days.
     */
    public void setDays( int days )
    {
        this.days = days;
    }


    /**
     * Sets the hour component.
     *
     * @param hours  the number of hours.
     */
    public void setHours( int hours )
    {
        this.hours = hours;
    }


    /**
     * Sets the minute component.
     *
     * @param minutes  the number of minutes.
     */
    public void setMinutes( int minutes )
    {
        this.minutes = minutes;
    }


    /**
     * Sets the second component.
     *
     * @param seconds  the number of seconds.
     */
    public void setSeconds( int seconds )
    {
        this.seconds = seconds;
    }


    // ── Write the Timetable Back into the Configuration ───────────────────────
    // We format each component as a two-digit zero-padded string and join them
    // with colons to produce the "dd:hh:mm:ss" token for the config file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the interval as a {@code "dd:hh:mm:ss"} string for use in the
     * OpenLDAP syncrepl directive.
     *
     * @return  the formatted interval string.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( intValToString( days ) );
        sb.append( ":" );
        sb.append( intValToString( hours ) );
        sb.append( ":" );
        sb.append( intValToString( minutes ) );
        sb.append( ":" );
        sb.append( intValToString( seconds ) );

        return sb.toString();

    }


    // ── Always Print with Two Digits ──────────────────────────────────────────
    // OpenLDAP requires two-digit components in the interval string — "01" not
    // "1" — so we zero-pad any value less than 10.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Formats an integer as a two-character zero-padded string.
     * Values below 10 get a leading zero; values 10 and above are printed as-is.
     *
     * @param val  the integer to format.
     * @return     the two-character string representation.
     */
    private String intValToString( int val )
    {
        if ( val < 10 )
        {
            return "0" + val;

        }
        else
        {
            return "" + val;
        }
    }
}
