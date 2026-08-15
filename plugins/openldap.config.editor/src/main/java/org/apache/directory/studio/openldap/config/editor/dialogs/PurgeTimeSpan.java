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
package org.apache.directory.studio.openldap.config.editor.dialogs;


import java.text.ParseException;


// Like C-3PO painstakingly reading the Jawa dialect to decode exactly
// what the Jawas are trying to say, we parse time span strings in the
// format "DD+HH:MM:SS" and translate them into structured days/hours/
// minutes/seconds values the rest of the system can actually use.
/**
 * Represents a time span used for purge age and purge interval in the
 * OpenLDAP AccessLog configuration. We parse strings like "2+12:30:00"
 * (2 days, 12 hours, 30 minutes, 0 seconds) and can also serialize back
 * to the same format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PurgeTimeSpan
{
    /** The days */
    protected int days = 0;

    /** The hours */
    protected int hours = 0;

    /** The minutes */
    protected int minutes = 0;

    /** The seconds */
    protected int seconds = 0;


    // Like C-3PO standing by with no messages to translate yet, we
    // initialize all time components to zero so the span starts out
    // as a clean, empty duration ready to be populated.
    /**
     * Creates a new PurgeTimeSpan with all components initialized to zero.
     */
    public PurgeTimeSpan()
    {
    }


    // Like C-3PO receiving an encoded message from the Jawas and immediately
    // starting the translation process, we accept a time span string and
    // delegate to parse() to decode it into days/hours/minutes/seconds.
    /**
     * Creates a new PurgeTimeSpan by parsing the given time span string.
     * The string format is {@code [DD+]HH:MM[:SS]}.
     *
     * @param s the time span string to parse
     * @throws ParseException if the string doesn't match the expected format
     */
    public PurgeTimeSpan( String s ) throws ParseException
    {
        parse( s );
    }


    // Like C-3PO receiving a pre-decoded briefing with all four time
    // components already worked out, we accept them directly after
    // validating each one so nothing out of range sneaks through.
    /**
     * Creates a new PurgeTimeSpan from explicit days, hours, minutes, and seconds
     * values. We validate each argument before storing it.
     *
     * @param days the number of days (0 to 99999)
     * @param hours the number of hours (0 to 23)
     * @param minutes the number of minutes (0 to 59)
     * @param seconds the number of seconds (0 to 59)
     * @throws IllegalArgumentException if any argument is out of its valid range
     */
    public PurgeTimeSpan( int days, int hours, int minutes, int seconds )
    {
        checkDaysArgument( days );
        checkHoursArgument( hours );
        checkMinutesSecondsArgument( minutes );
        checkMinutesSecondsArgument( seconds );

        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }


    // Like C-3PO checking whether the Jawa's claimed number of market days
    // is a realistic figure before accepting the translation, we validate
    // the days value and throw if it's outside the acceptable range.
    /**
     * Validates the days argument and throws if it's out of range (0-99999).
     *
     * @param value the days value to validate
     * @throws IllegalArgumentException if the value is out of the valid range
     */
    private void checkDaysArgument( int value ) throws IllegalArgumentException
    {
        if ( checkDays( value ) )
        {
            throw new IllegalArgumentException( "Days need to be comprised between 0 and 99999." );
        }
    }


    // Like C-3PO quietly checking his internal calendar to see if the
    // day count makes sense before committing to the translation, we
    // return true when the days value is outside the acceptable bounds
    // and false when it's valid.
    /**
     * Checks whether the given days value is out of the valid range (0-99999).
     *
     * @param value the days value to check
     * @return {@code true} if the value is out of range (invalid), {@code false} if it's valid
     */
    private boolean checkDays( int value )
    {
        return ( value < 0 ) || ( value > 99999 );
    }


    // Like C-3PO cross-referencing the Jawa's claimed hour count against
    // what a standard day actually contains, we validate hours and throw
    // if the value is outside the 0-23 range.
    /**
     * Validates the hours argument and throws if it's out of range (0-23).
     *
     * @param value the hours value to validate
     * @throws IllegalArgumentException if the value is outside 0-23
     */
    private void checkHoursArgument( int value )
    {
        if ( checkHours( value ) )
        {
            throw new IllegalArgumentException( "Hours, minutes, or seconds need to be comprised between 0 and 99999." );
        }
    }


    // Like C-3PO silently verifying that the hour in the Jawa's time code
    // falls within a standard clock range, we return true when hours are
    // out of bounds and false when they're fine.
    /**
     * Checks whether the given hours value is out of the valid range (0-23).
     *
     * @param value the hours value to check
     * @return {@code true} if the value is out of range (invalid), {@code false} if it's valid
     */
    private boolean checkHours( int value )
    {
        return ( value < 0 ) || ( value > 23 );
    }


    // Like C-3PO verifying that the Jawa's minute and second counts stay
    // within a realistic clock range before stamping the translation as
    // correct, we validate and throw for any value outside 0-59.
    /**
     * Validates a minutes or seconds argument and throws if it's out of range (0-59).
     *
     * @param value the minutes or seconds value to validate
     * @throws IllegalArgumentException if the value is outside 0-59
     */
    private void checkMinutesSecondsArgument( int value )
    {
        if ( checkMinutesSeconds( value ) )
        {
            throw new IllegalArgumentException( "Hours, minutes, or seconds need to be comprised between 0 and 99999." );
        }
    }


    // Like C-3PO quietly confirming the Jawa's minutes/seconds figure
    // is clock-legal before continuing the translation, we return true
    // when the value is out of range and false when it's acceptable.
    /**
     * Checks whether the given minutes or seconds value is out of range (0-59).
     *
     * @param value the minutes or seconds value to check
     * @return {@code true} if the value is out of range (invalid), {@code false} if it's valid
     */
    private boolean checkMinutesSeconds( int value )
    {
        return ( value < 0 ) || ( value > 59 );
    }


    // Like C-3PO slowly but methodically working through the Jawa dialect
    // character by character — parsing digits, plus signs, and colons —
    // we walk through the time span string and extract days, hours, minutes,
    // and seconds into their respective fields.
    /**
     * Parses a time span string in the format {@code [DD+]HH:MM[:SS]} and
     * populates the days/hours/minutes/seconds fields. We throw a
     * {@link ParseException} at the first sign of invalid content.
     *
     * @param s the string to parse
     * @throws ParseException if the string is null, too short, or contains illegal characters or out-of-range values
     */
    private void parse( String s ) throws ParseException
    {
        if ( s == null )
        {
            throw new ParseException( "The string is null.", 0 );
        }

        // Removing leading and trailing whitespaces
        s = s.trim();

        // Checking the minimum size of the string
        // It should be at least 5 chars ("HH:MM")
        if ( s.length() < 5 )
        {
            throw new ParseException( "The string is too short.", 0 );
        }

        // Initializing parsing objects
        int position = 0;
        char c;
        StringBuilder buffer = new StringBuilder();
        boolean hoursParsed = false;
        boolean minutesParsed = false;

        try
        {
            while ( ( position < s.length() ) )
            {
                c = s.charAt( position );

                // Figure
                if ( ( '0' <= c ) && ( c <= '9' ) )
                {
                    buffer.append( c );
                }
                // Plus sign
                else if ( '+' == c )
                {
                    int days = Integer.parseInt( buffer.toString() );

                    if ( checkDays( days ) )
                    {
                        throw new ParseException( "Days need to be comprised between 0 and 99999.", position );
                    }
                    else
                    {
                        this.days = days;
                    }

                    buffer = new StringBuilder();
                }
                // Colon sign
                else if ( ':' == c )
                {
                    if ( !hoursParsed )
                    {
                        int hours = Integer.parseInt( buffer.toString() );

                        if ( checkHours( hours ) )
                        {
                            throw new ParseException( "Hours need to be comprised between 0 and 23.", position );
                        }
                        else
                        {
                            this.hours = hours;
                        }

                        hoursParsed = true;
                        buffer = new StringBuilder();
                    }
                    else
                    {
                        int minutes = Integer.parseInt( buffer.toString() );

                        if ( checkMinutesSeconds( minutes ) )
                        {
                            throw new ParseException( "Minutes need to be comprised between 0 and 59.", position );
                        }
                        else
                        {
                            this.minutes = minutes;
                        }

                        minutesParsed = true;
                        buffer = new StringBuilder();
                    }
                }
                else
                {
                    throw new ParseException( "Illegal character", position );
                }

                position++;
            }
        }
        catch ( NumberFormatException e )
        {
            throw new ParseException( e.getMessage(), position );
        }

        if ( !hoursParsed )
        {
            throw new ParseException( "Hours need to be comprised between 0 and 23.", position );
        }
        else if ( !minutesParsed )
        {
            int minutes = Integer.parseInt( buffer.toString() );

            if ( checkMinutesSeconds( minutes ) )
            {
                throw new ParseException( "Minutes need to be comprised between 0 and 59.", position );
            }
            else
            {
                this.minutes = minutes;
            }
        }
        else
        {
            int seconds = Integer.parseInt( buffer.toString() );

            if ( checkMinutesSeconds( seconds ) )
            {
                throw new ParseException( "Seconds need to be comprised between 0 and 59.", position );
            }
            else
            {
                this.seconds = seconds;
            }
        }
    }


    // Like C-3PO retrieving the days field from the completed translation
    // so the caller can see how many days the Jawa actually meant, we
    // hand back the stored days value.
    /**
     * Returns the days component of this time span.
     *
     * @return the number of days
     */
    public int getDays()
    {
        return days;
    }


    // Like C-3PO handing back the hours portion of the decoded time code
    // so the caller knows how many hours are included in the span, we
    // return the stored hours value.
    /**
     * Returns the hours component of this time span.
     *
     * @return the number of hours (0-23)
     */
    public int getHours()
    {
        return hours;
    }


    // Like C-3PO reading out the minutes component of the decoded Jawa
    // time reference so the caller has the full picture, we return
    // the stored minutes value.
    /**
     * Returns the minutes component of this time span.
     *
     * @return the number of minutes (0-59)
     */
    public int getMinutes()
    {
        return minutes;
    }


    // Like C-3PO delivering the final seconds component of the time
    // code translation, we hand back the stored seconds value so
    // the caller can use the complete breakdown.
    /**
     * Returns the seconds component of this time span.
     *
     * @return the number of seconds (0-59)
     */
    public int getSeconds()
    {
        return seconds;
    }


    // Like C-3PO updating his translation notes when the Jawa revises
    // the number of market days, we replace the stored days value
    // with the new one.
    /**
     * Sets the days component of this time span.
     *
     * @param days the new number of days to store
     */
    public void setDays( int days )
    {
        this.days = days;
    }


    // Like C-3PO correcting the hours field in the translation when
    // the operator points out an error, we replace the stored hours value.
    /**
     * Sets the hours component of this time span.
     *
     * @param hours the new number of hours to store (0-23)
     */
    public void setHours( int hours )
    {
        this.hours = hours;
    }


    // Like C-3PO updating the minutes portion of the decoded time
    // when a correction comes in, we replace the stored minutes value.
    /**
     * Sets the minutes component of this time span.
     *
     * @param minutes the new number of minutes to store (0-59)
     */
    public void setMinutes( int minutes )
    {
        this.minutes = minutes;
    }


    // Like C-3PO revising the seconds value in the translation after
    // a correction, we update the stored seconds component.
    /**
     * Sets the seconds component of this time span.
     *
     * @param seconds the new number of seconds to store (0-59)
     */
    public void setSeconds( int seconds )
    {
        this.seconds = seconds;
    }


    // Like C-3PO delivering his completed translation in the format the
    // Jawas will recognize — reassembling days, hours, minutes, and seconds
    // back into the canonical "DD+HH:MM:SS" string — we serialize the
    // time span back to its wire format.
    /**
     * Returns the string representation of this time span in the format
     * {@code [DD+]HH:MM[:SS]}, omitting the days prefix when zero and
     * omitting the seconds suffix when zero.
     *
     * @return the formatted time span string
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Days (if needed)
        if ( days > 0 )
        {
            sb.append( days );
            sb.append( '+' );
        }

        // Hours
        sb.append( toString( hours ) );
        sb.append( ':' );

        // Minutes
        sb.append( toString( minutes ) );

        // Seconds (if needed)
        if ( seconds > 0 )
        {
            sb.append( ':' );
            sb.append( toString( seconds ) );
        }

        return sb.toString();
    }


    // Like C-3PO padding a single-digit time component with a leading zero
    // so the formatted output looks right — "03" instead of just "3" —
    // we ensure consistent two-digit formatting for time components.
    /**
     * Formats an integer time component as a two-character string,
     * padding with a leading zero if the value is less than 10.
     * Returns "00" for any value outside the 0-60 range.
     *
     * @param value the time component value to format
     * @return a two-character string representation
     */
    private String toString( int value )
    {
        if ( ( value < 0 ) || ( value > 60 ) )
        {
            return "00";
        }
        else if ( value < 10 )
        {
            return "0" + value;
        }
        else
        {
            return Integer.toString( value );
        }
    }

}
